package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/**
 * DTO genérico para préstamos, usado principalmente para operaciones administrativas.
 * Permite especificar todos los detalles del préstamo, incluyendo IDs de usuarios.
 */
public record LoanDTO(
    @NotNull(message = "El ID del juego es obligatorio")
    Long gameId,
    
    @NotNull(message = "El ID del prestador es obligatorio")
    Long lenderId,
    
    @NotNull(message = "El ID del prestatario es obligatorio")
    Long borrowerId,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime loanDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expectedReturnDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime returnDate,
    
    String notes
) {}
