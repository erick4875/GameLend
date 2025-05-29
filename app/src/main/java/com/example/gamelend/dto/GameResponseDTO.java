package com.example.gamelend.dto;

import com.example.gamelend.models.GameStatus;

public class GameResponseDTO {

    private Long id;
    private String title;
    private String platform;
    private String genre;
    private String description;
    private GameStatus status;
    private Long userId;
    private Long activeLoanId;
    private String userName;
    private Long imageId;
    private String imageUrl;
    private boolean catalog;
    private Long catalogGameId;

    // Constructor vacío
    public GameResponseDTO() {
    }

    // Constructor con todos los campos
    public GameResponseDTO(Long id, String title, String platform, String genre, String description,
                           GameStatus status, Long userId, String userName, Long imageId,
                           String imageUrl, boolean catalog, Long catalogGameId) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.userName = userName;
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
        this.title = title;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public boolean isCatalog() {
        return catalog;
    }

    public void setCatalog(boolean catalog) {
        this.catalog = catalog;
    }

    public Long getCatalogGameId() {
        return catalogGameId;
    }

    public void setCatalogGameId(Long catalogGameId) {
        this.catalogGameId = catalogGameId;
    }

    public Long getActiveLoanId() {
        return activeLoanId;
    }

    public void setActiveLoanId(Long activeLoanId) {
        this.activeLoanId = activeLoanId;
    }


}