package org.project.group5.gamelend.controller;

import java.io.IOException;
import java.util.List;

import org.project.group5.gamelend.dto.DocumentResponseDTO;
import org.project.group5.gamelend.dto.DocumentSummaryDTO;
import org.project.group5.gamelend.dto.DocumentUploadDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.mapper.DocumentMapper;
import org.project.group5.gamelend.repository.DocumentRepository;
import org.project.group5.gamelend.service.DocumentService;
import org.project.group5.gamelend.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de documentos.
 * Maneja operaciones CRUD y descarga/subida de archivos.
 */
@Slf4j
@RestController
@RequestMapping("api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;

    /**
     * Lista todos los documentos (solo admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentSummaryDTO>> list() {
        log.info("Solicitando lista de documentos");
        List<Document> documents = documentService.list();
        return documents.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(documentMapper.toSummaryDTOList(documents));
    }

    /**
     * Obtiene un documento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getDocumentFileById(@PathVariable Long id,
            HttpServletRequest request) {
        log.info("Solicitando contenido del documento ID: {}", id);
        Document document = documentService.find(id);

        // Determinar tipo y cargar recurso
        FileStorageService.ImageType imageType = documentService.determineImageType(document);
        Resource resource = fileStorageService.loadImageAsResource(document.getFileName(), imageType);

        // Validar y obtener tipo de contenido
        String contentType = document.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = documentService.determineContentType(request, document.getFileName(), document);
        }

        // Validar recurso
        if (resource == null || !resource.exists() || !resource.isReadable()) {
            log.warn("Recurso no encontrado o no legible, ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        // Obtener tamaño del contenido
        long contentLength = 0;
        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            log.warn("Error al obtener tamaño del contenido, ID: {}", id, e);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(resource);
    }

    /**
     * Sube un nuevo documento
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute @Valid DocumentUploadDTO uploadDTO) {
        log.info("Subiendo archivo: {} con metadatos: {}", file.getOriginalFilename(), uploadDTO);

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }

        Document savedDocument = documentService.save(file, uploadDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentMapper.toResponseDTO(savedDocument));
    }

    /**
     * Descarga un documento por nombre de archivo
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileByName(@PathVariable String fileName,
            HttpServletRequest request) {
        log.info("Solicitando descarga de archivo: {}", fileName);
        return documentService.download(fileName, request);
    }
}