package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar devoluciones de préstamos.
 */
public record LoanReturnDTO(
    @NotNull(message = "La fecha de devolución no puede ser nula")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime returnDate
) {
}

