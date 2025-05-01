package org.project.group5.gamelend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.repository.GameRepository;
import org.project.group5.gamelend.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de préstamos de juegos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;

    // Definir formatter para fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Crea un nuevo préstamo
     * 
     * @param loan Objeto préstamo a guardar
     * @return Préstamo guardado
     */
    public Loan saveLoan(Loan loan) {
        if (loan == null) {
            log.warn("Intento de guardar un préstamo nulo");
            throw new BadRequestException("El préstamo no puede ser nulo");
        }

        try {
            Loan saved = loanRepository.save(loan);
            log.info("Préstamo guardado correctamente con ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error al guardar el préstamo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el préstamo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los préstamos registrados
     * 
     * @return Lista de préstamos
     */
    public List<Loan> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos");
        }
        return loans;
    }

    /**
     * Obtiene un préstamo por su ID
     * 
     * @param id ID del préstamo
     * @return Préstamo encontrado
     */
    public Loan getLoanById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del préstamo no puede ser nulo");
        }

        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));
    }

    /**
     * Elimina un préstamo por su ID
     * 
     * @param id ID del préstamo a eliminar
     */
    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id); // Reutiliza el método que ya lanza excepciones
        loanRepository.delete(loan);
    }

    /**
     * Actualiza un préstamo existente
     * 
     * @param id   ID del préstamo a actualizar
     * @param loan Nuevos datos del préstamo
     * @return Préstamo actualizado
     */
    public Loan updateLoan(Long id, Loan loan) {
        if (!loanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Préstamo no encontrado con ID: " + id);
        }

        return loanRepository.save(loan);
    }

    /**
     * Registra la devolución de un préstamo
     * 
     * @param loan El préstamo con la fecha de devolución actualizada
     * @return Préstamo actualizado
     */
    public Loan returnLoan(Loan loan) {
        if (loan.getReturnDate() == null) {
            throw new BadRequestException("Fecha de devolución no puede ser nula");
        }

        return loanRepository.save(loan);
    }

    /**
     * Obtiene todos los préstamos y los convierte a DTOs para la respuesta
     * 
     * @return Lista de DTOs de préstamos
     */
    public List<LoanResponseDTO> getAllLoansDTO() {
        List<Loan> loans = getAllLoans();

        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos");
            return List.of(); // Lista vacía
        }

        // Convertir entidades a DTOs
        return loans.stream()
                .map(this::convertLoanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un préstamo por su ID y lo convierte a DTO
     * 
     * @param id ID del préstamo
     * @return DTO del préstamo
     */
    public LoanResponseDTO getLoanByIdDTO(Long id) {
        Loan loan = getLoanById(id);
        return convertLoanToDTO(loan);
    }

    /**
     * Guarda un préstamo y devuelve su DTO
     * 
     * @param loan Préstamo a guardar
     * @return DTO del préstamo guardado
     */
    public LoanResponseDTO saveLoanDTO(Loan loan) {
        Loan savedLoan = saveLoan(loan);
        return convertLoanToDTO(savedLoan);
    }

    /**
     * Actualiza un préstamo y devuelve su DTO
     * 
     * @param id   ID del préstamo a actualizar
     * @param loan Nuevos datos del préstamo
     * @return DTO del préstamo actualizado
     */
    public LoanResponseDTO updateLoanDTO(Long id, Loan loan) {
        Loan updatedLoan = updateLoan(id, loan);
        return convertLoanToDTO(updatedLoan);
    }

    /**
     * Registra la devolución de un préstamo y actualiza el estado del juego
     * 
     * @param loan El préstamo con la fecha de devolución actualizada
     * @return Préstamo actualizado
     */
    @Transactional
    public Loan returnLoanWithGameUpdate(Loan loan) {
        if (loan == null || loan.getId() == null) {
            throw new BadRequestException("El préstamo no puede ser nulo y debe tener un ID válido");
        }

        // Verificar que el préstamo existe
        Loan existingLoan = loanRepository.findById(loan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo con ID " + loan.getId() + " no existe"));

        // Verificar que no haya sido devuelto ya
        if (existingLoan.getReturnDate() != null) {
            log.warn("El préstamo con ID {} ya ha sido devuelto", loan.getId());
            throw new BadRequestException("El préstamo ya ha sido devuelto");
        }

        // Verificar que el juego existe
        Game game = existingLoan.getGame();
        if (game == null) {
            throw new BadRequestException("El préstamo no tiene un juego asociado");
        }

        // Actualizar la fecha de devolución
        existingLoan.setReturnDate(loan.getReturnDate());
        Loan updatedLoan = loanRepository.save(existingLoan);

        // Actualizar el estado del juego a DISPONIBLE
        game.setStatus(Game.GameStatus.AVAILABLE);
        gameRepository.save(game);

        log.info("Préstamo con ID {} devuelto correctamente", loan.getId());
        return updatedLoan;
    }

    /**
     * Registra la devolución de un préstamo y devuelve su respuesta DTO
     * 
     * @param loan El préstamo con la fecha de devolución actualizada
     * @return DTO del préstamo actualizado
     */
    public LoanResponseDTO returnLoanWithGameUpdateDTO(Loan loan) {
        Loan returnedLoan = returnLoanWithGameUpdate(loan);
        return convertLoanToDTO(returnedLoan);
    }

    /**
     * Método auxiliar para convertir un Loan a LoanResponseDTO usando patrón
     * Builder
     * 
     * @param loan Préstamo a convertir
     * @return DTO con la información del préstamo
     */
    private LoanResponseDTO convertLoanToDTO(Loan loan) {
        if (loan == null)
            return null;

        // Crear DTOs para las relaciones
        GameSummaryDTO gameSummary = null;
        if (loan.getGame() != null) {
            gameSummary = GameSummaryDTO.builder()
                    .id(loan.getGame().getId())
                    .title(loan.getGame().getTitle())
                    .build();
        }

        UserSummaryDTO borrowerSummary = null;
        if (loan.getBorrower() != null) {
            borrowerSummary = UserSummaryDTO.builder()
                    .id(loan.getBorrower().getId())
                    .publicName(loan.getBorrower().getPublicName())
                    .build();
        }

        UserSummaryDTO lenderSummary = null;

        if (loan.getLender() != null) {
            lenderSummary = UserSummaryDTO.builder()
                    .id(loan.getLender().getId())
                    .publicName(loan.getLender().getPublicName())
                    .build();
        } else if (loan.getGame() != null && loan.getGame().getUser() != null) {
            // Obtener el prestador del juego si no está establecido directamente
            lenderSummary = UserSummaryDTO.builder()
                    .id(loan.getGame().getUser().getId())
                    .publicName(loan.getGame().getUser().getPublicName())
                    .build();
        }

        // Construir el DTO completo
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .loanDate(loan.getLoanDate() != null ? loan.getLoanDate().format(DATE_FORMATTER) : null)
                .expectedReturnDate(
                        loan.getExpectedReturnDate() != null ? loan.getExpectedReturnDate().format(DATE_FORMATTER)
                                : null)
                .returnDate(loan.getReturnDate() != null ? loan.getReturnDate().format(DATE_FORMATTER) : null)
                .notes(loan.getNotes())
                .gameId(loan.getGame() != null ? loan.getGame().getId() : null)
                .gameTitle(loan.getGame() != null ? loan.getGame().getTitle() : null)
                .borrowerId(loan.getBorrower() != null ? loan.getBorrower().getId() : null)
                .borrowerName(loan.getBorrower() != null ? loan.getBorrower().getPublicName() : null)
                .status(loan.getReturnDate() != null ? "RETURNED" : "ACTIVE")
                .game(gameSummary)
                .borrower(borrowerSummary)
                .lender(lenderSummary)
                .build();
    }
}
