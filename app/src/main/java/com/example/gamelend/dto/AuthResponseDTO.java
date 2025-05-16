package com.example.gamelend.dto;

import java.util.List;

public class AuthResponseDTO {
    private String token;
    private String nombrePublico;
    private long userId;
    private List<String> roles;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getNombrePublico() {
        return nombrePublico;
    }
    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public List<String> getRoles() {
        return roles;
    }
}
