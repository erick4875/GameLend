package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

/**
 * DTO de respuesta para enviar información de juegos al cliente.
 * Contiene todos los datos necesarios para mostrar un juego.
 */
public record GameResponseDTO(
    /**
     * Identificador único del juego
     */
    Long id,

    /**
     * Título del juego
     */
    String title,

    /**
     * Sistema o consola donde se ejecuta
     */
    String platform,

    /**
     * Categoría o tipo de juego
     */
    String genre,

    /**
     * Información detallada del juego
     */
    String description,

    /**
     * Estado actual: AVAILABLE, BORROWED o UNAVAILABLE
     */
    GameStatus status,

    /**
     * ID del usuario propietario
     */
    Long userId,

    /**
     * Nombre público del propietario
     */
    String userName,

    /**
     * ID del documento de imagen asociado
     */
    Long imageId,

    /**
     * URL para acceder a la imagen
     */
    String imageUrl,

    /**
     * Indica si pertenece al catálogo general
     */
    boolean catalog,

    /**
     * ID del juego de catálogo relacionado
     */
    Long catalogGameId,

    /**
     * ID del préstamo activo, si existe
     */
    Long activeLoanId
) {
    // Los records generan automáticamente:
    // - Constructor
    // - Getters
    // - equals()
    // - hashCode()
    // - toString()
}
