package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DTO para datos de préstamo (entrada del cliente)
 * Usado para crear o actualizar préstamos
 *
 * @param id                 ID del préstamo (opcional para creación)
 * @param gameId             ID del juego prestado
 * @param lenderId           ID del usuario que presta
 * @param borrowerId         ID del usuario que toma prestado
 * @param loanDate           Fecha del préstamo (String en formato "yyyy-MM-dd'T'HH:mm:ss")
 * @param expectedReturnDate Fecha esperada de devolución (String, mismo formato)
 * @param returnDate         Fecha real de devolución (String, mismo formato, opcional)
 * @param notes              Notas adicionales sobre el préstamo (opcional)
 */
public record LoanDTO(
    Long id,
    Long gameId,
    Long lenderId,
    Long borrowerId,
    String loanDate,
    String expectedReturnDate,
    String returnDate,
    String notes
) {
    // Formato para parsear fechas desde String
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte `loanDate` (String) a `LocalDateTime`
     * Retorna null si el String es nulo o inválido
     */
    public LocalDateTime getLoanDateAsDateTime() {
        try {
            return loanDate != null ? LocalDateTime.parse(loanDate, DATE_FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convierte `expectedReturnDate` (String) a `LocalDateTime`
     * Retorna null si el String es nulo o inválido
     */
    public LocalDateTime getExpectedReturnDateAsDateTime() {
        try {
            return expectedReturnDate != null ? LocalDateTime.parse(expectedReturnDate, DATE_FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convierte `returnDate` (String) a `LocalDateTime`
     * Retorna null si el String es nulo o inválido
     */
    public LocalDateTime getReturnDateAsDateTime() {
        try {
            return returnDate != null ? LocalDateTime.parse(returnDate, DATE_FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
