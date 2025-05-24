package com.example.gamelend.dto;

public class LoanReturnDTO {

    private String returnDate; // Recibirá/enviará el LocalDateTime como String formateado

    // Constructor vacío
    public LoanReturnDTO() {
    }

    // Constructor para crear el objeto
    public LoanReturnDTO(String returnDate) {
        this.returnDate = returnDate;
    }

    // Getter
    public String getReturnDate() {
        return returnDate;
    }

    // Setter
    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}
