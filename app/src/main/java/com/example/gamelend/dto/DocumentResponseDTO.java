package com.example.gamelend.dto;

/**
 * DTO para recibir información completa de documentos desde el servidor.
 */
public class DocumentResponseDTO {

    private Long id;
    private String name;
    private String fileName;
    private String extension;
    private String completeFileName;
    private String status;
    private String urlFile;

    // Constructor vacío
    public DocumentResponseDTO() {
    }

    // Constructor con todos los campos
    public DocumentResponseDTO(Long id, String name, String fileName, String extension,
                               String completeFileName, String status, String urlFile) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.extension = extension;
        this.completeFileName = completeFileName;
        this.status = status;
        this.urlFile = urlFile;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public String getCompleteFileName() {
        return completeFileName;
    }

    public String getStatus() {
        return status;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setCompleteFileName(String completeFileName) {
        this.completeFileName = completeFileName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }
}
