package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO específico para registrar la devolución de un préstamo.
 * Se usa cuando un usuario devuelve un juego prestado.
 * Solo contiene la fecha de devolución, ya que es el único dato necesario para completar un préstamo.
 *
 * @param returnDate Fecha y hora real de devolución (formato "yyyy-MM-dd'T'HH:mm:ss")
 */
public record LoanReturnDTO(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime returnDate
) {
    // Los records generan automáticamente constructor, getters, equals(), hashCode() y toString().
}
