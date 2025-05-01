package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Game.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta de juego (servidor → cliente)
 * Muestra información detallada del juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResponseDTO {
    private Long id;
    private String title;
    private String platform;
    private String genre;
    private String description;
    private GameStatus status;  // Ahora usa directamente el enum de Game
    private Long userId;
    private String userName;
    
    // Campos para gestión de imágenes
    private Long imageId; 
    private String imagePath;   
    
    // Campos para relaciones de juegos de catálogo
    private boolean isCatalog; 
    private Long catalogGameId; 
}
