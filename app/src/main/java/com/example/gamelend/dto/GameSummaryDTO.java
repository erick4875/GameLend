package com.example.gamelend.dto;

import com.example.gamelend.models.GameStatus;

public class GameSummaryDTO {

    private Long id;
    private String title;
    private String platform;
    private GameStatus status;

    // Constructor vacío (necesario para Gson/Moshi)
    public GameSummaryDTO() {
    }

    // Constructor con todos los campos
    public GameSummaryDTO(Long id, String title, String platform, GameStatus status) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPlatform() {
        return platform;
    }

    public GameStatus getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}