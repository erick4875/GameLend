package com.example.gamelend.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TokenResponseDTO {

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    private Long userId;
    private String publicName;
    private List<String> roles;

    // Constructor vacío
    public TokenResponseDTO() {
    }

    // Constructor con todos los campos
    public TokenResponseDTO(String accessToken, String refreshToken, Long userId, String publicName, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.publicName = publicName;
        this.roles = roles;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPublicName() { return publicName; }
    public void setPublicName(String publicName) { this.publicName = publicName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
