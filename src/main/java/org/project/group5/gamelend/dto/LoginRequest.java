package org.project.group5.gamelend.dto;

/**
 * DTO (Data Transfer Object) para recibir los datos de login
 * Estructura simple para encapsular email y contraseña desde la petición
 */
public class LoginRequest {
    private String email;     // Email del usuario (identificador único)
    private String password;  // Contraseña en texto plano (se validará contra versión encriptada)

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

