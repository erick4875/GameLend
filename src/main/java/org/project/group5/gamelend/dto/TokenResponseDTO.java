package org.project.group5.gamelend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la respuesta de tokens
 * Contiene el token de acceso y el token de refresco
 */
public record TokenResponseDTO(
    
    @JsonProperty("access_token") // Token para autenticar peticiones
    String accessToken,
    @JsonProperty("refresh_token") // Token para obtener un nuevo token de acceso
    String refreshToken) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
