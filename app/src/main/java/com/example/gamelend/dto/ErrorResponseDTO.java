// En com.example.gamelend.dto.ErrorResponseDTO.java
package com.example.gamelend.dto;

import java.util.List;

public class ErrorResponseDTO {
    private List<String> details;
    private String error;
    private String message;
    private String timestamp;
    private int status;

    // Constructor vacío para Gson
    public ErrorResponseDTO() {}

    // Getters (y Setters si los necesitas)
    public List<String> getDetails() { return details; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
}
