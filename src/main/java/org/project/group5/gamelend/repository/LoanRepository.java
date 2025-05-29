package org.project.group5.gamelend.repository;

import java.util.List;

import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByLenderId(Long userId);

    List<Loan> findByBorrowerId(Long userId);

    List<Loan> findByReturnDateIsNull();

    /**
     * Verifica si existe un préstamo activo para un juego y usuario específicos.
     * Un préstamo se considera activo si su returnDate es null.
     *
     * @param game el juego a verificar
     * @param borrower el usuario prestatario
     * @return true si existe un préstamo activo, false en caso contrario
     */
    boolean existsByGameAndBorrowerAndReturnDateIsNull(Game game, User borrower);

    /**
     * Busca préstamos activos para un juego específico.
     *
     * @param gameId el ID del juego
     * @return lista de préstamos activos
     */
    List<Loan> findByGameIdAndReturnDateIsNull(Long gameId);
    List<Loan> findByGameAndLenderAndReturnDateIsNull(Game game, User lender);
}

