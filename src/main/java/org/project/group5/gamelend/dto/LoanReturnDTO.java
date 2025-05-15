package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.project.group5.gamelend.entity.Loan;

/**
 * DTO específico para registrar la devolución de un préstamo.
 * Se usa cuando un usuario devuelve un juego prestado.
 * Solo contiene la fecha de devolución, ya que es el único dato necesario
 * para completar un préstamo.
 */
public record LoanReturnDTO(
    String returnDate
) {
    // Formato estándar compartido con otros DTOs
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte la fecha de devolución de String a LocalDateTime
     * Devuelve null si el String es nulo, vacío o no se puede parsear
     */
    public LocalDateTime getReturnDateAsDateTime() {
        if (returnDate == null || returnDate.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(returnDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Aplica la fecha de devolución de este DTO a una entidad Loan existente
     * Si la fecha de devolución en el DTO es inválida o nula, no se realiza ninguna acción
     *
     * @param loan La entidad Loan a la que se aplicará la fecha de devolución
     */
    public void applyTo(Loan loan) {
        if (loan != null) {
            LocalDateTime parsedReturnDate = getReturnDateAsDateTime();
            if (parsedReturnDate != null) {
                loan.setReturnDate(parsedReturnDate);
            }
        }
    }

        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}
