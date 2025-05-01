package org.project.group5.gamelend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información básica de documentos (para listados)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSummaryDTO {
    private Long id;
    private String name;
    private String completeFileName;
    private String urlFile;
}
