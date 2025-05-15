package org.project.group5.gamelend.controller;

import java.io.IOException;
import java.util.List;

import org.project.group5.gamelend.dto.DocumentResponseDTO;
import org.project.group5.gamelend.dto.DocumentSummaryDTO;
import org.project.group5.gamelend.dto.DocumentUploadDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.mapper.DocumentMapper;
import org.project.group5.gamelend.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@RestController
@RequestMapping("api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;

    /**
     * Listar todos los documentos (resumen)
     */
    @GetMapping
    public ResponseEntity<List<DocumentSummaryDTO>> list() {
        log.info("Solicitando lista de documentos");
        List<Document> documents = documentService.list();

        if (documents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(documentMapper.toSummaryDTOList(documents));
    }

    /**
     * Buscar documento por ID (información completa)
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> find(@PathVariable Long id) {
        log.info("Buscando documento con ID: {}", id);
        Document document = documentService.find(id);
        return ResponseEntity.ok(documentMapper.toResponseDTO(document));
    }

    /**
     * Subir y guardar un archivo usando DocumentUploadDTO para metadatos.
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDTO> save(
            @RequestParam MultipartFile file,
            @ModelAttribute @Valid DocumentUploadDTO uploadDTO) throws IOException {

        log.info("Subiendo archivo: {} con metadatos: {}", file.getOriginalFilename(), uploadDTO);
        // Simplificado: Spring ya asegura que 'file' no es null si es requerido.
        // Solo verificamos si el archivo está vacío.
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no puede estar vacío");
        }

        // *** IMPORTANTE: Ajustar el servicio ***
        // *** IMPORTANTE: Ajustar el servicio ***
        // El método documentService.save ahora debería aceptar DocumentUploadDTO
        Document savedDocument = documentService.save(file, uploadDTO);

        log.info("Archivo guardado correctamente con ID: {}", savedDocument.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentMapper.toResponseDTO(savedDocument));
    }

    /**
     * Descargar un archivo por nombre
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Solicitando descarga de archivo: {}", fileName);

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de archivo requerido");
        }

        return documentService.download(fileName, request);
    }
}