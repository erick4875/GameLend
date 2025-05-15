package org.project.group5.gamelend.service;

import java.io.IOException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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

    /**
     * Lista todos los documentos disponibles.
     */
    public List<Document> list() {
        log.debug("Listando todos los documentos");
        return documentRepository.findAll();
    }

    /**
     * Busca un documento por su ID.
     */
    public Document find(Long id) {
        if (id == null) {
            log.warn("ID de documento nulo");
            throw new IllegalArgumentException("ID no puede ser nulo");
        }
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    /**
     * Guarda un nuevo documento a partir de un archivo y metadatos DTO.
     */
    public Document save(MultipartFile file, DocumentUploadDTO uploadDTO) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo no puede estar vacío");
        }
        if (uploadDTO == null) {
            throw new BadRequestException("Los metadatos del documento son requeridos");
        }

        ImageType imageType = determineImageTypeFromName(uploadDTO.name());

        try {
            String storedFileName = fileStorageService.storeImage(file, imageType);
            String originalFileNameNullable = file.getOriginalFilename();
            String originalFileName = (originalFileNameNullable == null) ? "" : StringUtils.cleanPath(originalFileNameNullable);
            String extension = getFileExtension(originalFileName);

            Document document = new Document();
            document.setName(uploadDTO.name());
            document.setFileName(storedFileName);
            document.setExtension(extension);
            document.setStatus("A");
            document.setDeleted(false);
            document.setLocalPath(getLocalPath(imageType, storedFileName));
            document.setImage(null);

            Document saved = documentRepository.save(document);
            log.info("Documento guardado correctamente con ID: {}", saved.getId());
            return saved;

        } catch (FileStorageException e) {
            log.error("Error al guardar el archivo para el documento '{}': {}", uploadDTO.name(), e.getMessage());
            throw e;
        }
    }

    /**
     * Genera una respuesta para descargar un documento.
     */
    public ResponseEntity<Resource> download(String fileName, HttpServletRequest request) {
        if (fileName == null || fileName.isEmpty()) {
            throw new BadRequestException("Nombre de archivo vacío o nulo");
        }

        log.info("Descargando documento: {}", fileName);

        Document document = documentRepository.findByFileName(fileName);
        if (document == null) {
            throw new ResourceNotFoundException("Document not found with file name: " + fileName);
        }

        ImageType imageType = determineImageType(document);
        Resource resource = fileStorageService.loadImageAsResource(document.getFileName(), imageType);
        String contentType = determineContentType(request, document.getFileName(), document);

        String downloadFileName = document.getCompleteFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                .body(resource);
    }

    /**
     * Elimina un documento por su ID (marca como eliminado y elimina el archivo físico).
     */
    public void delete(Long id) {
        Document document = find(id);

        ImageType imageType = determineImageType(document);
        boolean fileDeleted = fileStorageService.deleteImage(document.getFileName(), imageType);

        if (!fileDeleted) {
            log.warn("No se pudo eliminar el archivo físico: {} para el documento ID {}", document.getFileName(), id);
        }

        document.setDeleted(true);
        document.setStatus("D");
        documentRepository.save(document);

        log.info("Documento con ID {} marcado como eliminado", id);
    }

    /**
     * Determina el tipo de imagen a partir del nombre del documento.
     */
    private ImageType determineImageTypeFromName(String documentName) {
        if (documentName != null && documentName.toLowerCase().contains("game")) {
            return ImageType.GAME;
        } else {
            return ImageType.USER;
        }
    }

    /**
     * Determina el tipo de imagen a partir del documento.
     */
    private ImageType determineImageType(Document document) {
        return determineImageTypeFromName(document.getName());
    }

    /**
     * Obtiene la extensión del archivo.
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            int dotIndex = filename.lastIndexOf(".");
            if (filename.length() - dotIndex - 1 <= 10) {
                return filename.substring(dotIndex + 1).toLowerCase();
            } else {
                log.warn("Extensión del archivo original '{}' demasiado larga, truncando a 10 caracteres.", filename);
                return filename.substring(dotIndex + 1, dotIndex + 11).toLowerCase();
            }
        }
        return "";
    }

    /**
     * Obtiene la ruta local donde se almacena el archivo según el tipo.
     */
    private String getLocalPath(ImageType type, String fileName) {
        return switch (type) {
            case GAME -> "games/" + fileName;
            case USER -> "users/" + fileName;
        };
    }

    /**
     * Determina el tipo MIME del archivo para la descarga.
     */
    private String determineContentType(HttpServletRequest request, String storedFileName, Document document) {
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(storedFileName);
            log.debug("Tipo MIME del ServletContext para {}: {}", storedFileName, contentType);
        } catch (Exception e) {
            log.warn("No se pudo obtener el tipo MIME del ServletContext para {}: {}", storedFileName, e.getMessage());
        }

        if (contentType == null && document.getExtension() != null && !document.getExtension().isEmpty()) {
            String extension = document.getExtension().toLowerCase();
            log.debug("Determinando tipo MIME basado en la extensión: {}", extension);
            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                contentType = "image/jpeg";
            } else {
                log.warn("Extensión no esperada encontrada: {}, usando application/octet-stream", extension);
                contentType = "application/octet-stream";
            }
        }

        if (contentType == null) {
            log.warn("No se pudo determinar el tipo MIME para {}, usando application/octet-stream", storedFileName);
            contentType = "application/octet-stream";
        }

        log.info("Tipo MIME determinado para {}: {}", storedFileName, contentType);
        return contentType;
    }
}