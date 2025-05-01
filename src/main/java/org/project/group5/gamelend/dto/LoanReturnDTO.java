package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.project.group5.gamelend.entity.Loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO específico para registrar la devolución de un préstamo.
 * Se usa cuando un usuario devuelve un juego prestado.
 * Solo contiene la fecha de devolución, ya que es el único dato necesario
 * para completar un préstamo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanReturnDTO {
    private String returnDate;

    // Formato estándar compartido con otros DTOs
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Método de utilidad para obtener la fecha como LocalDateTime
    public LocalDateTime getReturnDateAsDateTime() throws DateTimeParseException {
        return returnDate != null && !returnDate.isEmpty()
                ? LocalDateTime.parse(returnDate, DATE_FORMATTER)
                : null;
    }

    // Método para aplicar esta devolución a un préstamo existente
    public void applyTo(Loan loan) throws DateTimeParseException {
        if (loan != null && this.returnDate != null && !this.returnDate.isEmpty()) {
            loan.setReturnDate(LocalDateTime.parse(this.returnDate, DATE_FORMATTER));
        }
    }
}
