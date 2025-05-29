package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record LoanResponseDTO(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime loanDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expectedReturnDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime returnDate,
    String notes,
    GameSummaryDTO game,
    UserSummaryDTO lender,
    UserSummaryDTO borrower
) {}