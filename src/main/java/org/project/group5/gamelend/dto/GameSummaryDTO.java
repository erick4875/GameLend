package org.project.group5.gamelend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado de Juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSummaryDTO {
    private Long id;
    private String title;
    private String platform;
    private String status;

}