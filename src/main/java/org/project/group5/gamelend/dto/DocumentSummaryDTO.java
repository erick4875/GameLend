package org.project.group5.gamelend.dto;

/**
 * DTO con información básica de documentos
 * Contiene datos inmutables resumidos para enviar al cliente
 *
 * @param id               Identificador único del documento
 * @param name             Nombre descriptivo del documento
 * @param completeFileName Nombre original del archivo subido
 * @param urlFile          URL para descargar o visualizar el archivo
 */
public record DocumentSummaryDTO(
        Long id,
        String name,
        String completeFileName,
        String urlFile
) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
