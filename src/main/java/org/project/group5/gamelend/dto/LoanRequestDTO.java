package org.project.group5.gamelend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitudes de préstamos (cliente -> servidor).
 * Contiene la información mínima necesaria para crear un préstamo.
 */
public record LoanRequestDTO(
    @NotNull(message = "El ID del juego es obligatorio")
    Long gameId,
    String notes
) {}
