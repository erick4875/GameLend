package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para respuestas de error.
 * Proporciona una estructura uniforme para todos los errores de la API.
 */
public record ErrorResponse(
    /**
     * Código de estado HTTP (ej: 400, 404, 500)
     */
    int status,

    /**
     * Mensaje descriptivo del error
     */
    String message,

    /**
     * Momento exacto en que ocurrió el error
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {}