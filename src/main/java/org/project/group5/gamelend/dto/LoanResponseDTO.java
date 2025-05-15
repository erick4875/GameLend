package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para respuestas de préstamos (servidor -> cliente).
 * Incluye información completa del préstamo, juego y usuarios involucrados.
 *
 * @param id                 ID del préstamo
 * @param loanDate           Fecha y hora del préstamo
 * @param expectedReturnDate Fecha y hora esperada de devolución
 * @param returnDate         Fecha y hora real de devolución
 * @param notes              Notas adicionales sobre el préstamo
 * @param game               Resumen del juego prestado
 * @param lender             Resumen del usuario prestamista
 * @param borrower           Resumen del usuario prestatario
 */
public record LoanResponseDTO(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime loanDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expectedReturnDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime returnDate,
    String notes,
    GameSummaryDTO game,
    UserSummaryDTO lender,
    UserSummaryDTO borrower
) {
    // Los records generan automáticamente constructor, getters, equals(), hashCode() y toString().
}
