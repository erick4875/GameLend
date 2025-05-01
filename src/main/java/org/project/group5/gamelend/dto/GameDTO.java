package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar datos de juego (entrada del cliente)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDTO {
    private Long id;            // ID del juego (opcional, para actualizaciones)
    private String title;
    private String platform;
    private String genre;
    private String description;
    private GameStatus status;
    private Long userId;
    private Long imageId;        // Referencia al ID de la imagen si ya existe
    private String imagePath;    // O ruta a la imagen
    private boolean isCatalog;   // Si es un juego de catálogo
    private Long catalogGameId;  // ID del juego de catálogo relacionado, si aplica
}
