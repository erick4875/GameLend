package com.example.gamelend.dto;

public class LoanDTO {

    private Long id;
    private Long gameId;
    private Long lenderId;
    private Long borrowerId;
    private String loanDate; // Recibirá/enviará el LocalDateTime como String formateado
    private String expectedReturnDate; // Recibirá/enviará el LocalDateTime como String formateado
    private String returnDate; // Recibirá/enviará el LocalDateTime como String formateado
    private String notes;

    // Constructor vacío (necesario para Gson/Moshi)
    public LoanDTO() {
    }

    // Constructor con todos los campos (útil para crear instancias)
    public LoanDTO(Long id, Long gameId, Long lenderId, Long borrowerId,
                   String loanDate, String expectedReturnDate, String returnDate, String notes) {
        this.id = id;
        this.gameId = gameId;
        this.lenderId = lenderId;
        this.borrowerId = borrowerId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnDate = returnDate;
        this.notes = notes;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getLenderId() {
        return lenderId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public String getLoanDate() {
        return loanDate;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public void setLenderId(Long lenderId) {
        this.lenderId = lenderId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public void setLoanDate(String loanDate) {
        this.loanDate = loanDate;
    }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
