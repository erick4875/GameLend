package org.project.group5.gamelend.dto;

/**
 * DTO para representar un resumen de usuario
 * Se utiliza para enviar datos de usuario simplificados a través de la API
 *
 * @param id         Identificador único del usuario
 * @param publicName Nombre público del usuario, usado para mostrar
 */
public record UserSummaryDTO(
    Long id,
    String publicName
) {
    // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}