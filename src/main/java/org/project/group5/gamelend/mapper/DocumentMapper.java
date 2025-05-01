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
     * Convierte un Document a DocumentResponseDTO
     */
    public DocumentResponseDTO toResponseDTO(Document document) {
        if (document == null) {
            return null;
        }
        
        return DocumentResponseDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .fileName(document.getFileName())
                .extension(document.getExtension())
                .completeFileName(document.getCompleteFileName())
                .status(document.getStatus())
                .urlFile(createDownloadUrl(document.getFileName()))
                .build();
    }
    
    /**
     * Convierte una lista de Document a una lista de DocumentResponseDTO
     */
    public List<DocumentResponseDTO> toResponseDTOList(List<Document> documents) {
        if (documents == null) {
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
        
        return DocumentSummaryDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .completeFileName(document.getCompleteFileName())
                .urlFile(createDownloadUrl(document.getFileName()))
                .build();
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
        return "/api/documents/download/" + fileName;
    }
}
