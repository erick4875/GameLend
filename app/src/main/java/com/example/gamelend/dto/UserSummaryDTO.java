package com.example.gamelend.dto;

/**
 * DTO para recibir información resumida de un usuario desde el servidor.
 */
public class UserSummaryDTO {

    private Long id;
    private String publicName;

    // Constructor vacío
    public UserSummaryDTO() {
    }

    // Constructor con todos los campos
    public UserSummaryDTO(Long id, String publicName) {
        this.id = id;
        this.publicName = publicName;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPublicName() {
        return publicName;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }
}
