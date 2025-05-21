package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

/**
 * DTO de respuesta de juego (servidor hacia cliente)
 * Muestra información detallada del juego
 *
 * @param id            ID del juego
 * @param title         Título
 * @param platform      Plataforma
 * @param genre         Género
 * @param description   Descripción
 * @param status        Estado (AVAILABLE, BORROWED o UNAVAILABLE)
 * @param userId        ID del propietario
 * @param userName      Nombre público del propietario
 * @param imageId       ID de la imagen (documento)
 * @param imageUrl     Ruta/URL de la imagen
 * @param catalog     Si es juego de catálogo general
 * @param catalogGameId ID del juego de catálogo vinculado (opcional)
 */
public record GameResponseDTO(
        Long id,
        String title,
        String platform,
        String genre,
        String description,
        GameStatus status,
        Long userId,
        String userName,
        Long imageId,
        String imageUrl,
        boolean catalog,
        Long catalogGameId
) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
