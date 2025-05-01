package org.project.group5.gamelend.controller;

import java.io.IOException;
import java.util.List;

import org.project.group5.gamelend.dto.DocumentResponseDTO;
import org.project.group5.gamelend.dto.DocumentSummaryDTO;
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

import jakarta.servlet.http.HttpServletRequest;
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
     * Subir y guardar un archivo
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDTO> save(
            @RequestParam MultipartFile file,
            @ModelAttribute Document documentMetadata) throws IOException {

        log.info("Subiendo archivo: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        Document savedDocument = documentService.save(file, documentMetadata);
        log.info("Archivo guardado correctamente: {}", file.getOriginalFilename());
        
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