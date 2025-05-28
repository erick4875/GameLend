package org.project.group5.gamelend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.DocumentResponseDTO;
import org.project.group5.gamelend.dto.DocumentSummaryDTO;
import org.project.group5.gamelend.entity.Document;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades Document y sus DTOs.
 * Esta es una implementación manual, no usa MapStruct directamente para los métodos de DTO.
 */
@Component // Marcado como un bean de Spring
public class DocumentMapper {

    // No se necesita imageBaseUrl aquí si solo se construyen URLs relativas
    // o si la URL completa se construye en el servicio/controlador.

    /**
     * Convierte un Document a DocumentResponseDTO.
     */
    public DocumentResponseDTO toResponseDTO(Document document) {
        if (document == null) {
            return null;
        }
        return new DocumentResponseDTO(
                document.getId(),
                document.getName(),
                document.getFileName(),
                document.getExtension(),
                document.getOriginalFileNameForDownload(), // <-- CORRECCIÓN AQUÍ
                document.getStatus(),
                createDownloadUrl(document.getFileName()) // Usa el nombre de archivo almacenado para la URL
        );
    }

    /**
     * Convierte una lista de Document a una lista de DocumentResponseDTO.
     */
    public List<DocumentResponseDTO> toResponseDTOList(List<Document> documents) {
        if (documents == null) {
            return Collections.emptyList(); // Devuelve lista vacía en lugar de null
        }
        return documents.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Document a DocumentSummaryDTO.
     */
    public DocumentSummaryDTO toSummaryDTO(Document document) {
        if (document == null) {
            return null;
        }
        return new DocumentSummaryDTO(
                document.getId(),
                document.getName(),
                document.getOriginalFileNameForDownload(), // <-- CORRECCIÓN AQUÍ
                createDownloadUrl(document.getFileName()) // Usa el nombre de archivo almacenado para la URL
        );
    }

    /**
     * Convierte una lista de Document a una lista de DocumentSummaryDTO.
     */
    public List<DocumentSummaryDTO> toSummaryDTOList(List<Document> documents) {
        if (documents == null) {
            return Collections.emptyList(); // Devuelve lista vacía en lugar de null
        }
        return documents.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genera la URL de descarga relativa para un documento.
     * @param storedFileName El nombre del archivo tal como está guardado en el sistema de archivos (ej. UUID.jpg).
     * @return La URL relativa para descargar el archivo.
     */
    private String createDownloadUrl(String storedFileName) {
        if (storedFileName == null || storedFileName.isEmpty()) {
            return null;
        }
        // Esta es la ruta relativa al endpoint de descarga en DocumentController
        return "/api/documents/download/" + storedFileName;
    }

    // Si necesitaras mapear desde un DTO a una entidad Document (para creación/actualización),
    // lo añadirías aquí, similar a como lo hace MapStruct, pero manualmente.
    // Por ejemplo:
    /*
    public Document toEntity(DocumentUploadDTO dto) {
        if (dto == null) return null;
        return Document.builder()
                .name(dto.name())
                // Otros campos se establecerían en el servicio (fileName, contentType, etc.)
                .build();
    }
    */
}
