package org.project.group5.gamelend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas con información completa de documentos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {
    private Long id;
    private String name;
    private String fileName;
    private String extension;
    private String completeFileName;
    private String status;
    private String urlFile;   
}
