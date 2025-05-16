package com.example.gamelend.dto;

import com.google.gson.annotations.SerializedName;

public class GameDTO {
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("platform")
    private String platform;

    @SerializedName("genre")
    private String genre;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("imageId")
    private Long imageId;

    @SerializedName("imagePath")
    private String imagePath;

    @SerializedName("catalog")
    private Boolean catalog;

    @SerializedName("catalogGameId")
    private Long catalogGameId;

    // Constructor
    public GameDTO(Long id, String title, String platform, String genre, String description, String status, Long userId, Long imageId, String imagePath, Boolean catalog, Long catalogGameId) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.imageId = imageId;
        this.imagePath = imagePath;
        this.catalog = catalog;
        this.catalogGameId = catalogGameId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
