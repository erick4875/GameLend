package com.example.gamelend.dto;

public class LoanResponseDTO {

    private Long id;
    private String loanDate; // Recibirá el LocalDateTime como String formateado
    private String expectedReturnDate; // Recibirá el LocalDateTime como String formateado
    private String returnDate; // Recibirá el LocalDateTime como String formateado
    private String notes;
    private GameSummaryDTO game; // Usará tu POJO GameSummaryDTO de Android
    private UserSummaryDTO lender; // Usará tu POJO UserSummaryDTO de Android
    private UserSummaryDTO borrower; // Usará tu POJO UserSummaryDTO de Android

    // Constructor vacío (necesario para Gson/Moshi)
    public LoanResponseDTO() {
    }

    // Constructor con todos los campos (útil para crear instancias o para testing)
    public LoanResponseDTO(Long id, String loanDate, String expectedReturnDate, String returnDate,
                           String notes, GameSummaryDTO game, UserSummaryDTO lender, UserSummaryDTO borrower) {
        this.id = id;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returnDate = returnDate;
        this.notes = notes;
        this.game = game;
        this.lender = lender;
        this.borrower = borrower;
    }

    // Getters
    public Long getId() {
        return id;
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

    public GameSummaryDTO getGame() {
        return game;
    }

    public UserSummaryDTO getLender() {
        return lender;
    }

    public UserSummaryDTO getBorrower() {
        return borrower;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
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

    public void setGame(GameSummaryDTO game) {
        this.game = game;
    }

    public void setLender(UserSummaryDTO lender) {
        this.lender = lender;
    }

    public void setBorrower(UserSummaryDTO borrower) {
        this.borrower = borrower;
    }
}
