package com.example.gamelend.dto;

import com.google.gson.annotations.SerializedName;

public class GameSummaryDTO {
    private Long id;
    private String title;
    private String platform;
    private String status;

    // Constructor
    public GameSummaryDTO(Long id, String title, String platform, String status) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
