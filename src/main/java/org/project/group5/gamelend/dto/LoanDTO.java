package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/**
 * DTO genérico para préstamos.
 * Usado principalmente para:
 * - Operaciones administrativas
 * - Creación y actualización de préstamos
 * - Transferencia de datos completos del préstamo
 */
public record LoanDTO(
    /**
     * ID del juego a prestar
     */
    @NotNull(message = "El ID del juego es obligatorio")
    Long gameId,
    
    /**
     * ID del usuario que presta el juego
     */
    @NotNull(message = "El ID del prestador es obligatorio")
    Long lenderId,
    
    /**
     * ID del usuario que recibe el préstamo
     */
    @NotNull(message = "El ID del prestatario es obligatorio")
    Long borrowerId,
    
    /**
     * Fecha y hora de inicio del préstamo
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime loanDate,
    
    /**
     * Fecha y hora prevista de devolución
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expectedReturnDate,
    
    /**
     * Fecha y hora real de devolución
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime returnDate,
    
    /**
     * Notas o comentarios sobre el préstamo
     */
    String notes
) {}
