package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DTO para respuestas de préstamos (servidor -> cliente)
 * Contiene información completa del préstamo con detalles del juego, prestamista y prestatario
 *
 * @param id                 ID del préstamo
 * @param loanDate           Fecha del préstamo (String "yyyy-MM-dd'T'HH:mm:ss")
 * @param expectedReturnDate Fecha esperada de devolución (String "yyyy-MM-dd'T'HH:mm:ss")
 * @param returnDate         Fecha real de devolución (String "yyyy-MM-dd'T'HH:mm:ss", opcional)
 * @param notes              Notas adicionales sobre el préstamo
 * @param game               Resumen del juego prestado
 * @param lender             Resumen del usuario prestamista
 * @param borrower           Resumen del usuario prestatario
 */
public record LoanResponseDTO(
    Long id,
    String loanDate,
    String expectedReturnDate,
    String returnDate,
    String notes,

    // Objetos DTO con detalles
    GameSummaryDTO game,
    UserSummaryDTO lender,
    UserSummaryDTO borrower
) {
    // Formato estándar para fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte `loanDate` (String) a `LocalDateTime`
     * Retorna null si es nulo o inválido
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
     * Retorna null si es nulo o inválido
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
     * Retorna null si es nulo o inválido
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
