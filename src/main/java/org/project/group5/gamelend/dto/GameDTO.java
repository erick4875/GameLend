package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferencia de datos de juegos.
 * Se usa para operaciones de creación y actualización.
 */
public record GameDTO(
    // Identificación
    Long id,

    /**
     * Título del juego
     * Requerido, longitud: 1-255 caracteres
     */
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 1, max = 255, message = "El título debe tener entre 1 y 255 caracteres")
    String title,

    /**
     * Plataforma donde se ejecuta el juego
     * Requerido, longitud: 1-100 caracteres
     */
    @NotBlank(message = "La plataforma no puede estar vacía")
    @Size(min = 1, max = 100, message = "La plataforma debe tener entre 1 y 100 caracteres")
    String platform,

    /**
     * Género o categoría del juego
     * Requerido, longitud: 1-100 caracteres
     */
    @NotBlank(message = "El género no puede estar vacío")
    @Size(min = 1, max = 100, message = "El género debe tener entre 1 y 100 caracteres")
    String genre,

    /**
     * Descripción detallada del juego
     * Opcional, máximo 2000 caracteres
     */
    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    String description,

    /**
     * Estado actual del juego (AVAILABLE, BORROWED, etc)
     * Requerido
     */
    @NotNull(message = "El estado del juego no puede ser nulo")
    GameStatus status,

    /**
     * ID del propietario del juego
     * Requerido, debe ser positivo
     */
    @Positive(message = "El ID del usuario debe ser positivo")
    @NotNull(message = "El ID del usuario no puede ser nulo")
    Long userId,

    /**
     * ID de la imagen asociada al juego
     * Opcional
     */
    Long imageId,

    /**
     * URL de la imagen del juego
     * Opcional, máximo 255 caracteres
     */
    @Size(max = 255, message = "La ruta de la imagen no puede exceder los 255 caracteres")
    String imageUrl,
    
    /**
     * Indica si es un juego del catálogo general
     * Opcional
     */
    Boolean catalog,

    /**
     * ID del juego de catálogo relacionado
     * Opcional
     */
    Long catalogGameId) {

    /**
     * Constructor compacto que:
     * - Elimina espacios en blanco del título
     * - Mantiene las validaciones de Bean Validation
     */
    public GameDTO {
        if (title != null) {
            title = title.trim();
        }
    }
}
