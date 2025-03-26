package org.project.group5.gamelend.dto;

/**
 * DTO para devolver el token y datos básicos del usuario
 * Contiene la información mínima necesaria para el cliente después de autenticación
 */
public class AuthResponse {
    private String token;           
    private String nombrePublico;   
    private Long userId;          

    // Constructor que inicializa todos los campos
    public AuthResponse(String token, String nombrePublico, Long userId) {
        this.token = token;
        this.nombrePublico = nombrePublico;
        this.userId = userId;
    }

    // Getters (no se necesitan setters ya que es una respuesta inmutable)
    public String getToken() {
        return token;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

    public Long getUserId() {
        return userId;
    }
}