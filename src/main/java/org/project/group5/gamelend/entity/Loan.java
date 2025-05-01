package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "lender_id")
    private User lender;

    @ManyToOne
    @JoinColumn(name = "borrower_id")
    private User borrower;

    @Column(name = "loan_date")
    private LocalDateTime loanDate;

    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;
    
    @Column(name = "notes", length = 500)
    private String notes;

    // El constructor personalizado ya no es necesario con @Builder
    // pero puedes mantenerlo si lo usas en alguna parte del código
    public Loan(User lender, User borrower, Game game,
                LocalDateTime loanDate, LocalDateTime returnDate) {
        this.lender = lender;
        this.borrower = borrower;
        this.game = game;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }
}

