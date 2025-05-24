package com.example.gamelend.dto;

/**
 * DTO para enviar las credenciales de inicio de sesión al servidor.
 */
public class LoginRequestDTO {

    private String email;
    private String password;

    // Constructor vacío
    public LoginRequestDTO() {
    }

    // Constructor para crear el objeto antes de enviarlo
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}