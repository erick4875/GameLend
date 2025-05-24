package org.project.group5.gamelend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la respuesta de tokens
 * Contiene el token de acceso y el token de refresco
 */
public record TokenResponseDTO(
    
    @JsonProperty("access_token") // Token para autenticar peticiones
    String accessToken,
    @JsonProperty("refresh_token") // Token para obtener un nuevo token de acceso
    String refreshToken,
    Long userId,
    String publicName,
    List<String> roles
    ){}
