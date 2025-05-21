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

/**
 * Entidad que representa un préstamo de un juego entre usuarios
 */
@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    /**
     * Identificador único del préstamo
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Juego que ha sido prestado
     * Relación muchos a uno: muchos préstamos pueden referirse al mismo juego (a lo largo del tiempo)
     */
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     * Usuario que presta el juego (propietario)
     * Relación muchos a uno: un usuario puede realizar muchos préstamos
     */
    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    /**
     * Usuario que toma prestado el juego
     * Relación muchos a uno: un usuario puede tomar prestados muchos juegos
     */
    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    /**
     * Fecha y hora en que se realizó el préstamo
     */
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    /**
     * Fecha y hora esperada para la devolución del juego
     */
    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;

    /**
     * Fecha y hora real en que se devolvió el juego
     * Será nulo si el juego aún no ha sido devuelto
     */
    @Column(name = "return_date")
    private LocalDateTime returnDate;
    
    /**
     * Notas adicionales sobre el préstamo 
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Constructor para crear un préstamo con todos los campos obligatorios
     * @param lender Usuario que presta el juego
     * @param borrower Usuario que toma prestado el juego
     * @param game Juego prestado
     * @param loanDate Fecha y hora del préstamo
     * @param returnDate Fecha y hora de la devolución (puede ser nulo si aún no se ha devuelto)
     */
    public Loan(User lender, User borrower, Game game,
                LocalDateTime loanDate, LocalDateTime returnDate) {
        this.lender = lender;
        this.borrower = borrower;
        this.game = game;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    /**
     * Verifica si el préstamo está actualmente activo (es decir, el juego ha sido prestado pero aún no devuelto)
     * @return true si el juego no ha sido devuelto, false en caso contrario
     */
    public boolean isActive() {
        return this.returnDate == null;
    }
}

