package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para datos de juego (entrada del cliente).
 * Usado para crear o actualizar juegos.
 *
 * @param id            ID del juego (opcional para creación, usado en
 *                      actualización)
 * @param title         Título del juego (requerido)
 * @param platform      Plataforma del juego (requerido)
 * @param genre         Género del juego (requerido)
 * @param description   Descripción del juego (opcional)
 * @param status        Estado del juego (requerido)
 * @param userId        ID del usuario propietario (requerido)
 * @param imageId       ID de la imagen asociada (opcional)
 * @param imageUrl     Ruta a imagen preexistente (opcional)
 * @param catalog       Indica si es un juego de catálogo general (opcional)
 * @param catalogGameId ID del juego de catálogo vinculado (opcional)
 */
public record GameDTO(
        Long id,

        @NotBlank(message = "El título no puede estar vacío") @Size(min = 1, max = 255, message = "El título debe tener entre 1 y 255 caracteres") String title,

        @NotBlank(message = "La plataforma no puede estar vacía") @Size(min = 1, max = 100, message = "La plataforma debe tener entre 1 y 100 caracteres") String platform,

        @NotBlank(message = "El género no puede estar vacío") @Size(min = 1, max = 100, message = "El género debe tener entre 1 y 100 caracteres") String genre,

        @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres") String description,

        @NotNull(message = "El estado del juego no puede ser nulo") GameStatus status,

        @Positive(message = "El ID del usuario debe ser positivo") @NotNull(message = "El ID del usuario no puede ser nulo") Long userId,

        Long imageId,
        @Size(max = 255, message = "La ruta de la imagen no puede exceder los 255 caracteres") String imageUrl,
        
        Boolean catalog,
        Long catalogGameId) {

    // Records generan automáticamente: constructor, getters, equals(), hashCode(),
    // toString()

    // Constructor compacto para lógica de inicialización o validación adicional si
    // es necesario
    public GameDTO {
        if (title != null) {
            title = title.trim();
        }
    }
}
