package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO específico para representar un préstamo.
 * Se usa para recibir datos de préstamo a través de la API.
 * Entrada de datos (cliente -> servidor)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {
    private Long id;
    private Long gameId;
    private Long lenderId;
    private Long borrowerId;
    private String loanDate;
    private String expectedReturnDate;
    private String returnDate;
    private String notes;               

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
