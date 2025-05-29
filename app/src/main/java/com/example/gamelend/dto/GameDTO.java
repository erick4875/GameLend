package com.example.gamelend.dto;

import com.example.gamelend.models.GameStatus;

public class GameDTO {

    private Long id;
    private String title;
    private String platform;
    private String genre;
    private String description;
    private GameStatus status;
    private Long userId;
    private Long imageId;
    private String imageUrl;
    private Boolean catalog;
    private Long catalogGameId;

    // Constructor vacío
    public GameDTO() {
    }

    // Constructor con todos los campos
    public GameDTO(Long id, String title, String platform, String genre, String description,
                   GameStatus status, Long userId, Long imageId, String imageUrl,
                   Boolean catalog, Long catalogGameId) {
        this.id = id;
        this.setTitle(title); // Usar el setter si quieres la lógica de trim
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.catalog = catalog;
        this.catalogGameId = catalogGameId;
    }

    // Getters y Setters
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
        // lógica del constructor del record
        if (title != null) {
            this.title = title.trim();
        } else {
            this.title = null;
        }
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getCatalog() {
        return catalog;
    }

    public void setCatalog(Boolean catalog) {
        this.catalog = catalog;
    }

    public Long getCatalogGameId() {
        return catalogGameId;
    }

    public void setCatalogGameId(Long catalogGameId) {
        this.catalogGameId = catalogGameId;
    }

}

