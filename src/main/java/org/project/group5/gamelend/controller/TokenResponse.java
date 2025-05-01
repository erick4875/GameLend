package org.project.group5.gamelend.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

// devuelve al cliente el token de acceso y el token de refresco
// el token de acceso es el que se usa para autenticar las peticiones al servidor
public record TokenResponse(
    
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken) {
}
