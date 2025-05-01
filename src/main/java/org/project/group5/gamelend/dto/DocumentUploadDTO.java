package org.project.group5.gamelend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir información al subir un documento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadDTO {
    private String name;
    private String description;
}
