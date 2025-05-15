package org.project.group5.gamelend.repository;

import java.util.List;

import org.project.group5.gamelend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con préstamos de juegos
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Encuentra préstamos donde el usuario indicado es el prestador
     * @param userId ID del usuario prestador
     * @return Lista de préstamos realizados por el usuario
     */
    @Query("SELECT l FROM Loan l WHERE l.lender.id = :userId")
    List<Loan> findByLenderId(@Param("userId") Long userId);

    /**
     * Encuentra préstamos donde el usuario indicado es el receptor
     * @param userId ID del usuario receptor
     * @return Lista de préstamos recibidos por el usuario
     */
    @Query("SELECT l FROM Loan l WHERE l.borrower.id = :userId")
    List<Loan> findByBorrowerId(@Param("userId") Long userId);

    /**
     * Encuentra préstamos activos (sin fecha de devolución)
     * @return Lista de préstamos activos
     */
    @Query("SELECT l FROM Loan l WHERE l.returnDate IS NULL")
    List<Loan> findActiveLoans();

    /**
     * Encuentra préstamos activos de un juego específico
     * @param gameId ID del juego
     * @return Lista de préstamos activos del juego
     */
    @Query("SELECT l FROM Loan l WHERE l.game.id = :gameId AND l.returnDate IS NULL")
    List<Loan> findActiveLoansByGameId(@Param("gameId") Long gameId);

    /**
     * Verifica si un juego está actualmente prestado
     * @param gameId ID del juego
     * @return true si el juego está prestado, false en caso contrario
     */
    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.game.id = :gameId AND l.returnDate IS NULL")
    boolean isGameBorrowed(@Param("gameId") Long gameId);

    /**
     * Encuentra todos los préstamos para un juego específico que aún no han sido devueltos.
     * @param gameId El ID del juego.
     * @return Una lista de préstamos activos para el juego.
     */
    List<Loan> findByGameIdAndReturnDateIsNull(Long gameId);
}
