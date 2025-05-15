package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para la creación o actualización de préstamos.
 *
 * @param id                 ID del préstamo (opcional para creación)
 * @param gameId             ID del juego prestado
 * @param lenderId           ID del usuario que presta
 * @param borrowerId         ID del usuario que recibe el préstamo
 * @param loanDate           Fecha y hora del préstamo 
 * @param expectedReturnDate Fecha y hora esperada de devolución
 * @param returnDate         Fecha y hora real de devolución
 * @param notes              Notas adicionales sobre el préstamo (opcional)
 */
public record LoanDTO(
        Long id,
        Long gameId,
        Long lenderId,
        Long borrowerId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime loanDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expectedReturnDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime returnDate,
        String notes
) {
    // Los records generan automáticamente constructor, getters, equals(), hashCode() y toString().
}
