package com.example.gamelend.dto;

/**
 * DTO para recibir información resumida de documentos desde el servidor.
 */
public class DocumentSummaryDTO {

    private Long id;
    private String name;
    private String completeFileName;
    private String urlFile;

    // Constructor vacío
    public DocumentSummaryDTO() {
    }

    // Constructor con todos los campos
    public DocumentSummaryDTO(Long id, String name, String completeFileName, String urlFile) {
        this.id = id;
        this.name = name;
        this.completeFileName = completeFileName;
        this.urlFile = urlFile;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompleteFileName() {
        return completeFileName;
    }

    public String getUrlFile() {
        return urlFile;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompleteFileName(String completeFileName) {
        this.completeFileName = completeFileName;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }
}
