package org.project.group5.gamelend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir información al subir un documento
 * Contiene los metadatos asociados a un archivo subido
 *
 * @param name        Nombre descriptivo para el documento. No debe estar vacío
 * @param description Descripción opcional del documento
 */
public record DocumentUploadDTO(
        @NotBlank(message = "El nombre del documento no puede estar vacío")
        @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
        String name,

        // Descripción opcional del documento
        @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
        String description
) {
    // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
