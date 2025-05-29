package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.project.group5.gamelend.dto.DocumentUploadDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.FileStorageException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.repository.DocumentRepository;
import org.project.group5.gamelend.service.FileStorageService.ImageType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de documentos (subida, descarga, eliminación).
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<Document> list() {
        log.debug("Listando todos los documentos");
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Document find(Long id) {
        if (id == null) {
            log.warn("ID de documento nulo al intentar buscar.");
            throw new BadRequestException("El ID del documento no puede ser nulo.");
        }
        return documentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Documento no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Documento no encontrado con ID: " + id);
                });
    }

    public Document save(MultipartFile file, DocumentUploadDTO uploadDTO) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo no puede estar vacío.");
        }
        if (uploadDTO == null) {
            throw new BadRequestException("Los metadatos del documento (uploadDTO) son requeridos.");
        }

        ImageType imageType = determineImageTypeFromName(
                uploadDTO.name() != null ? uploadDTO.name() : file.getOriginalFilename());

        try {
            String storedFileName = fileStorageService.storeImage(file, imageType);

            String originalFileName = StringUtils
                    .cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file");
            String extension = getFileExtension(originalFileName);
            String documentName = uploadDTO.name() != null ? uploadDTO.name() : originalFileName;

            Document document = Document.builder()
                    .name(documentName)
                    .fileName(storedFileName)
                    .contentType(file.getContentType())
                    .extension(extension)
                    .size(file.getSize())
                    .uploadDate(LocalDateTime.now())
                    .status("A")
                    .deleted(false)
                    .localPath(fileStorageService.getLocalPath(imageType, storedFileName))
                    .build();

            Document saved = documentRepository.save(document);
            log.info("Documento guardado correctamente: '{}', ID: {}", saved.getName(), saved.getId());
            return saved;

        } catch (FileStorageException e) {
            log.error("Error al guardar el archivo para el documento '{}': {}",
                    (uploadDTO.name() != null ? uploadDTO.name() : file.getOriginalFilename()), e.getMessage());
            throw new FileStorageException("No se pudo guardar el archivo: " +
                    (file.getOriginalFilename() != null ? file.getOriginalFilename() : "nombre desconocido"), e);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(String fileName, HttpServletRequest request) {
        if (fileName == null || fileName.isEmpty()) {
            throw new BadRequestException("Nombre de archivo vacío o nulo.");
        }
        log.info("Descargando documento con nombre de archivo físico: {}", fileName);

        Document document = documentRepository.findByFileName(fileName);
        if (document == null) {
            log.warn("Documento no encontrado con nombre de archivo físico: {}", fileName);
            throw new ResourceNotFoundException("Documento no encontrado: " + fileName);
        }

        ImageType imageType = determineImageType(document);
        Resource resource = fileStorageService.loadImageAsResource(document.getFileName(), imageType);
        String contentType = determineContentType(request, document.getFileName(), document);

        String downloadFileName = document.getOriginalFileNameForDownload();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                .body(resource);
    }

    public void deleteDocument(Long documentId) {
        if (documentId == null) {
            log.warn("Intento de eliminar documento con ID nulo.");
            return;
        }
        Document document = documentRepository.findById(documentId)
                .orElse(null);

        if (document != null) {
            log.info("Iniciando eliminación para Documento ID: {}, FileName: {}", document.getId(),
                    document.getFileName());
            ImageType imageType = determineImageType(document);
            try {
                boolean filePhysicallyDeleted = fileStorageService.deleteImage(document.getFileName(), imageType);
                if (filePhysicallyDeleted) {
                    log.info("Archivo físico {} eliminado correctamente.", document.getFileName());
                } else {
                    log.warn("No se pudo eliminar el archivo físico: {} para el documento ID {}",
                            document.getFileName(), documentId);
                }
            } catch (Exception e) {
                log.error("Excepción al intentar eliminar el archivo físico para el documento ID {}: {}", documentId,
                        e.getMessage(), e);
            }

            documentRepository.deleteById(documentId);
            log.info("Entidad Document eliminada de la BD con ID: {}", documentId);

        } else {
            log.warn("No se encontró Document con ID: {} para eliminar.", documentId);
        }
    }

    public void delete(Long id) {
        Document document = find(id);

        ImageType imageType = determineImageType(document);
        boolean fileDeleted = fileStorageService.deleteImage(document.getFileName(), imageType);

        if (!fileDeleted) {
            log.warn("No se pudo eliminar el archivo físico: {} para el documento ID {} durante el soft delete.",
                    document.getFileName(), id);
        }

        document.setDeleted(true);
        document.setStatus("D");
        documentRepository.save(document);

        log.info("Documento con ID {} marcado como eliminado (soft delete).", id);
    }

    public ImageType determineImageTypeFromName(String documentNameOrOriginalFileName) {
        if (documentNameOrOriginalFileName != null) {
            String lowerName = documentNameOrOriginalFileName.toLowerCase();
            if (lowerName.contains("profile_image_user") || lowerName.contains("user")) {
                return FileStorageService.ImageType.USER;
            } else if (lowerName.contains("game_image") || lowerName.contains("game")) {
                return FileStorageService.ImageType.GAME;
            }
        }
        log.warn("No se pudo determinar un tipo de imagen específico para '{}', usando USER por defecto.",
                documentNameOrOriginalFileName);
        return FileStorageService.ImageType.USER;
    }

    public ImageType determineImageType(Document document) {
        // Asegúrate que este método sea público si es llamado desde fuera de esta clase
        // (ej. DocumentController)
        return determineImageTypeFromName(document.getName() != null ? document.getName() : document.getFileName());
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }


    public String determineContentType(HttpServletRequest request, String storedFileName, Document document) {
        // Asegúrate que este método sea público si es llamado desde fuera de esta clase
        // (ej. DocumentController)
        String contentType = null;
        if (document.getContentType() != null && !document.getContentType().isBlank()) {
            return document.getContentType();
        }
        if (request != null && request.getServletContext() != null) {
            try {
                contentType = request.getServletContext().getMimeType(storedFileName);
            } catch (Exception e) {
                log.warn("No se pudo obtener el tipo MIME del ServletContext para {}: {}", storedFileName,
                        e.getMessage());
            }
        }
        if (contentType == null && document.getExtension() != null && !document.getExtension().isEmpty()) {
            String extension = document.getExtension().toLowerCase();
            switch (extension) {
                case "jpg":
                case "jpeg":
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                    break;
                case "png":
                    contentType = MediaType.IMAGE_PNG_VALUE;
                    break;
                case "gif":
                    contentType = MediaType.IMAGE_GIF_VALUE;
                    break;
                default:
                    log.warn("Extensión no mapeada a tipo MIME: {}, usando application/octet-stream", extension);
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                    break;
            }
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        log.info("Tipo MIME determinado para {}: {}", storedFileName, contentType);
        return contentType;
    }
}