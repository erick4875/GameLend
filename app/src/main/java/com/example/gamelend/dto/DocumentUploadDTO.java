package com.example.gamelend.dto;

/**
 * DTO para enviar metadatos al subir un documento desde la app Android.
 */
public class DocumentUploadDTO {

    private String name;
    private String description;

    // Constructor vacío
    public DocumentUploadDTO() {
    }

    // Constructor con todos los datos
    public DocumentUploadDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
