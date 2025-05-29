package com.example.gamelend.dto;

// Android
import java.util.List;

/**
 * DTO para recibir la respuesta de tokens (acceso y refresco) desde el servidor.
 */
public class AuthResponseDTO {

    private String token;
    private String publicName;
    private Long userId;
    private List<String> roles;

    // Constructor vacío
    public AuthResponseDTO() {
    }

    // Constructor con todos los campos
    public AuthResponseDTO(String token, String publicName, Long userId, List<String> roles) {
        this.token = token;
        this.publicName = publicName;
        this.userId = userId;
        this.roles = roles;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getPublicName() {
        return publicName;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}