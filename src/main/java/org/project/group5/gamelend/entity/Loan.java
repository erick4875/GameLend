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
 * Entidad que representa un préstamo de juego entre usuarios.
 * Gestiona el ciclo de vida completo de un préstamo desde su creación hasta su devolución.
 */
@Entity
@Table(name = "loans")
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
     * Juego prestado
     * Un juego puede tener múltiples préstamos a lo largo del tiempo
     */
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     * Usuario que presta el juego (propietario)
     * Un prestador puede tener múltiples préstamos activos
     */
    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    /**
     * Usuario que recibe el préstamo
     * Un prestatario puede tener múltiples préstamos activos
     */
    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    /**
     * Fecha y hora de inicio del préstamo
     */
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    /**
     * Fecha y hora prevista de devolución
     * Ayuda a gestionar los plazos de préstamo
     */
    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;

    /**
     * Fecha y hora real de devolución
     * null indica que el préstamo sigue activo
     */
    @Column(name = "return_date")
    private LocalDateTime returnDate;
    
    /**
     * Notas o comentarios sobre el préstamo
     * Máximo 500 caracteres
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Constructor con campos obligatorios
     * 
     * @param lender Usuario que presta el juego
     * @param borrower Usuario que recibe el préstamo
     * @param game Juego a prestar
     * @param loanDate Fecha de inicio
     * @param returnDate Fecha de devolución (null si activo)
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
     * Verifica si el préstamo está activo
     * 
     * @return true si no ha sido devuelto, false si ya finalizó
     */
    public boolean isActive() {
        return this.returnDate == null;
    }
}

