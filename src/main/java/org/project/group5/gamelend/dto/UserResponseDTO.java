package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para enviar datos de usuario en respuestas de la API
 * No incluye información sensible como la contraseña
 *
 * @param id               ID del usuario
 * @param publicName       Nombre público del usuario
 * @param email            Correo electrónico del usuario
 * @param registrationDate Fecha de registro
 * @param province         Provincia del usuario
 * @param city             Ciudad del usuario
 * @param games            Lista de juegos que posee el usuario (DTOs de respuesta)
 * @param gamesLent        Lista de juegos que el usuario ha prestado (DTOs resumidos)
 * @param roles            Lista de nombres de roles asignados al usuario
 */
public record UserResponseDTO(
    Long id,
    String publicName,
    String email,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime registrationDate,
    String province,
    String city,
    List<GameResponseDTO> games,
    List<GameSummaryDTO> gamesLent,
    List<String> roles
) {
    // Constructor canónico para asegurar que las listas no sean nulas
    public UserResponseDTO {
        if (games == null) {
            games = new ArrayList<>();
        }
        if (gamesLent == null) {
            gamesLent = new ArrayList<>();
        }
        if (roles == null) {
            roles = new ArrayList<>();
        }
    }

    // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
