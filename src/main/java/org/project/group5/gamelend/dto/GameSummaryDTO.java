package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

/**
 * DTO simplificado de Juego para listados o vistas resumidas
 *
 * @param id       ID del juego
 * @param title    Título del juego
 * @param platform Plataforma del juego
 * @param status   Estado del juego (AVAILABLE, BORROWED o UNAVAILABLE)
 * 
 */
public record GameSummaryDTO(
                Long id,
                String title,
                String platform,
                GameStatus status) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(),
        // toString()
}