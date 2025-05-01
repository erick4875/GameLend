package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar información completa de un préstamo en respuestas.
 * Salida de datos (servidor -> cliente)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDTO {
    private Long id;
    private String loanDate;
    private String expectedReturnDate;
    private String returnDate;
    private String notes;

    // Datos del juego
    private Long gameId;
    private String gameTitle;

    // Datos del prestador/prestatario
    private Long borrowerId;
    private String borrowerName;

    // Estado del préstamo
    private String status;

    // Datos completos (relaciones)
    private GameSummaryDTO game;
    private UserSummaryDTO lender;
    private UserSummaryDTO borrower;

    // Formato estándar para fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte la fecha de préstamo de String a LocalDateTime
     */
    public LocalDateTime getLoanDateAsDateTime() {
        return loanDate != null ? LocalDateTime.parse(loanDate, DATE_FORMATTER) : null;
    }

    /**
     * Convierte la fecha esperada de devolución de String a LocalDateTime
     */
    public LocalDateTime getExpectedReturnDateAsDateTime() {
        return expectedReturnDate != null ? LocalDateTime.parse(expectedReturnDate, DATE_FORMATTER) : null;
    }

    /**
     * Convierte la fecha de devolución de String a LocalDateTime
     */
    public LocalDateTime getReturnDateAsDateTime() {
        return returnDate != null ? LocalDateTime.parse(returnDate, DATE_FORMATTER) : null;
    }
}
