package org.project.group5.gamelend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.DocumentResponseDTO;
import org.project.group5.gamelend.dto.DocumentSummaryDTO;
import org.project.group5.gamelend.entity.Document;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversión entre entidades Document y sus DTOs
 */
@Component
public class DocumentMapper {

    /**
     * Convierte un Document a DocumentResponseDTO (constructor de record)
     */
    public DocumentResponseDTO toResponseDTO(Document document) {
        if (document == null) {
            return null;
        }

        // Usar el constructor del record
        return new DocumentResponseDTO(
                document.getId(),
                document.getName(),
                document.getFileName(),
                document.getExtension(),
                document.getCompleteFileName(),
                document.getStatus(),
                createDownloadUrl(document.getFileName()));
    }

    /**
     * Convierte una lista de Document a una lista de DocumentResponseDTO
     */
    public List<DocumentResponseDTO> toResponseDTOList(List<Document> documents) {
        if (documents == null) {
            // Si la lista es nula, devuelve una lista vacía
            return List.of();
        }

        return documents.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Document a DocumentSummaryDTO
     */
    public DocumentSummaryDTO toSummaryDTO(Document document) {
        if (document == null) {
            return null;
        }

        return new DocumentSummaryDTO(
                document.getId(),
                document.getName(),
                document.getCompleteFileName(),
                createDownloadUrl(document.getFileName()));

    }

    /**
     * Convierte una lista de Document a una lista de DocumentSummaryDTO
     */
    public List<DocumentSummaryDTO> toSummaryDTOList(List<Document> documents) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genera la URL de descarga para un documento
     */
    private String createDownloadUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // Considera usar UriComponentsBuilder para construir URLs de forma más segura
        return "/api/documents/download/" + fileName;
    }
}
