package org.project.group5.gamelend.service;

import java.io.IOException;
import java.util.List;

import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.exception.FileStorageException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.repository.DocumentRepository;
import org.project.group5.gamelend.service.FileStorageService.ImageType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    /**
     * Lista todos los documentos disponibles
     */
    public List<Document> list() {
        log.debug("Listando todos los documentos");
        return documentRepository.findAll();
    }

    /**
     * Busca un documento por su ID
     */
    public Document find(Long id) {
        if (id == null) {
            log.warn("ID de documento nulo");
            throw new IllegalArgumentException("ID no puede ser nulo");
        }

        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo documento
     */
    public Document save(MultipartFile file, Document document) throws IOException {
        validateFileAndDocument(file, document);
        
        // Determinar el tipo de imagen basado en los metadatos
        ImageType imageType = determineImageType(document);
        
        try {
            // Almacenar el archivo usando FileStorageService
            String storedFileName = fileStorageService.storeImage(file, imageType);
            
            // Actualizar metadatos del documento
            document.setFileName(storedFileName);
            document.setExtension(getFileExtension(file));
            document.setStatus("A"); // Activo
            document.setDeleted(false);
            document.setLocalPath(getLocalPath(imageType, storedFileName));
            
            // No almacenamos el contenido binario, solo la referencia
            document.setImage(null);
            
            Document saved = documentRepository.save(document);
            log.info("Documento guardado correctamente con ID: {}", saved.getId());
            return saved;
        } catch (FileStorageException e) {
            log.error("Error al guardar documento: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Genera una respuesta para descargar un documento
     */
    public ResponseEntity<Resource> download(String fileName, HttpServletRequest request) {
        if (fileName == null || fileName.isEmpty()) {
            log.warn("Nombre de archivo vacío o nulo");
            throw new IllegalArgumentException("Nombre de archivo vacío o nulo");
        }

        log.info("Descargando documento: {}", fileName);

        Document document = documentRepository.findByFileName(fileName);
        if (document == null) {
            log.warn("Documento no encontrado: {}", fileName);
            throw new ResourceNotFoundException("Documento no encontrado: " + fileName);
        }

        // Determinar tipo de imagen
        ImageType imageType = determineImageType(document);
        
        // Obtener recurso desde el sistema de archivos
        Resource resource = fileStorageService.loadImageAsResource(fileName, imageType);
        
        // Determinar el tipo de contenido
        String contentType = determineContentType(request, fileName, document);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
    
    /**
     * Elimina un documento por su ID
     */
    public void delete(Long id) {
        Document document = find(id);
        
        // Eliminar archivo físico
        ImageType imageType = determineImageType(document);
        boolean fileDeleted = fileStorageService.deleteImage(document.getFileName(), imageType);
        
        if (!fileDeleted) {
            log.warn("No se pudo eliminar el archivo físico: {}", document.getFileName());
        }
        
        // Marcar como eliminado en base de datos o eliminar registro
        document.setDeleted(true);
        document.setStatus("D"); // Deleted
        documentRepository.save(document);
        
        log.info("Documento con ID {} marcado como eliminado", id);
    }

    // ----- Métodos auxiliares -----

    private void validateFileAndDocument(MultipartFile file, Document document) {
        if (file == null || file.isEmpty()) {
            log.warn("Archivo vacío o nulo");
            throw new IllegalArgumentException("Archivo vacío o nulo");
        }

        if (document == null) {
            log.warn("Datos del documento nulos");
            throw new IllegalArgumentException("Datos del documento nulos");
        }
    }
    
    private ImageType determineImageType(Document document) {
        // Lógica para determinar el tipo basada en metadatos del documento
        // Ejemplo: basado en el nombre o uso previsto
        if (document.getName() != null && document.getName().toLowerCase().contains("game")) {
            return ImageType.GAME;
        } else {
            return ImageType.USER;
        }
    }
    
    private String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "";
    }
    
    private String getLocalPath(ImageType type, String fileName) {
        return switch(type) {
            case GAME -> "games/" + fileName;
            case USER -> "users/" + fileName;
            default -> fileName;
        };
    }
    
    private String determineContentType(HttpServletRequest request, String fileName, Document document) {
        // Intentar obtener el tipo MIME del contexto
        String contentType = request.getServletContext().getMimeType(fileName);
        
        // Si no se puede determinar, usar la extensión del documento
        if (contentType == null && document.getExtension() != null) {
            contentType = switch(document.getExtension().toLowerCase()) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "pdf" -> "application/pdf";
                default -> "application/octet-stream";
            };
        }
        
        // Valor predeterminado si todo lo demás falla
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return contentType;
    }
}