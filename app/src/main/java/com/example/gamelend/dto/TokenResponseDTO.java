package com.example.gamelend.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList; // Para inicializar listas

public class TokenResponseDTO {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    private Long userId;
    private String publicName;
    private String email;
    private List<String> roles;

    // Constructor vacío
    public TokenResponseDTO() {
        this.roles = new ArrayList<>(); // Inicializar para evitar null
    }

    // Constructor con todos los campos
    public TokenResponseDTO(String accessToken, String refreshToken, Long userId, String publicName, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.publicName = publicName;
        this.email = email; // <--- AÑADIDO AL CONSTRUCTOR
        this.roles = (roles != null) ? roles : new ArrayList<>();
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Long getUserId() { return userId; }
    public String getPublicName() { return publicName; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }

    // Setters
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setPublicName(String publicName) { this.publicName = publicName; }
    public void setEmail(String email) { this.email = email; }
    public void setRoles(List<String> roles) { this.roles = (roles != null) ? roles : new ArrayList<>(); }
}