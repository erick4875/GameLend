package com.example.gamelend.dto;

public class LoanRequestDTO {
    private Long gameId;
    public LoanRequestDTO() {
    }

    public LoanRequestDTO(Long gameId) {
        this.gameId = gameId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    // Getters y setters para otros campos si los añades
}
