package org.project.group5.gamelend.dto;

/**
 * DTO para respuestas con información completa de documentos
 * Contiene los datos inmutables que se envían al cliente sobre un documento
 *
 * @param id               ID del documento
 * @param name             Nombre descriptivo
 * @param fileName         Nombre del archivo almacenado
 * @param extension        Extensión del archivo
 * @param completeFileName Nombre original del archivo
 * @param status           Estado (ej: A - Activo)
 * @param urlFile          URL del archivo
 */
public record DocumentResponseDTO(
        Long id,
        String name,
        String fileName,
        String extension,
        String completeFileName,
        String status,
        String urlFile
) {

            // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()

}
