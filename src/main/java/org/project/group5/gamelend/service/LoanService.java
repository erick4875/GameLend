package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.LoanMapper;
import org.project.group5.gamelend.repository.GameRepository;
import org.project.group5.gamelend.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de préstamos de juegos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;
    private final LoanMapper loanMapper;
    private final UserService userService;
    private final GameService gameService;

    /**
     * Guarda un préstamo.
     */
    public Loan saveLoan(Loan loan) {
        if (loan == null) throw new BadRequestException("El préstamo no puede ser nulo");
        return loanRepository.save(loan);
    }

    /**
     * Obtiene todos los préstamos.
     */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Obtiene un préstamo por ID.
     */
    public Loan getLoanById(Long id) {
        if (id == null) throw new BadRequestException("El ID del préstamo no puede ser nulo");
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));
    }

    /**
     * Elimina un préstamo y actualiza el estado del juego si es necesario.
     */
    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        Game game = loan.getGame();
        if (game != null && game.getStatus() == Game.GameStatus.BORROWED) {
            boolean otroPrestamoActivo = loanRepository.findByGameIdAndReturnDateIsNull(game.getId())
                    .stream().anyMatch(l -> !l.getId().equals(id));
            if (!otroPrestamoActivo) {
                game.setStatus(Game.GameStatus.AVAILABLE);
                gameRepository.save(game);
            }
        }
        loanRepository.delete(loan);
    }

    /**
     * Actualiza un préstamo con otra entidad Loan.
     */
    public Loan updateLoan(Long id, Loan loan) {
        Loan existingLoan = getLoanById(id);
        existingLoan.setLoanDate(loan.getLoanDate());
        existingLoan.setExpectedReturnDate(loan.getExpectedReturnDate());
        existingLoan.setReturnDate(loan.getReturnDate());
        existingLoan.setNotes(loan.getNotes());
        existingLoan.setGame(loan.getGame());
        existingLoan.setLender(loan.getLender());
        existingLoan.setBorrower(loan.getBorrower());
        return loanRepository.save(existingLoan);
    }

    /**
     * Marca un préstamo como devuelto (solo actualiza la fecha).
     */
    public Loan returnLoan(Loan loan) {
        if (loan.getReturnDate() == null) throw new BadRequestException("Fecha de devolución no puede ser nula");
        Loan existingLoan = getLoanById(loan.getId());
        existingLoan.setReturnDate(loan.getReturnDate());
        return loanRepository.save(existingLoan);
    }

    /**
     * Obtiene todos los préstamos como DTO.
     */
    public List<LoanResponseDTO> getAllLoansDTO() {
        return loanRepository.findAll().stream()
                .map(this::convertLoanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un préstamo por ID como DTO.
     */
    public LoanResponseDTO getLoanByIdDTO(Long id) {
        return convertLoanToDTO(getLoanById(id));
    }

    /**
     * Guarda un préstamo y lo devuelve como DTO.
     */
    public LoanResponseDTO saveLoanDTO(Loan loan) {
        return convertLoanToDTO(saveLoan(loan));
    }

    /**
     * Actualiza un préstamo y lo devuelve como DTO.
     */
    public LoanResponseDTO updateLoanDTO(Long id, Loan loan) {
        return convertLoanToDTO(updateLoan(id, loan));
    }

    /**
     * Marca un préstamo como devuelto y actualiza el estado del juego.
     */
    @Transactional
    public Loan returnLoanWithGameUpdate(Loan loan) {
        if (loan == null || loan.getId() == null) throw new BadRequestException("El préstamo no puede ser nulo y debe tener un ID válido");
        Loan existingLoan = loanRepository.findById(loan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo con ID " + loan.getId() + " no existe"));
        if (existingLoan.getReturnDate() != null) throw new BadRequestException("El préstamo ya ha sido devuelto");
        Game game = existingLoan.getGame();
        if (game == null) throw new BadRequestException("El préstamo no tiene un juego asociado");
        existingLoan.setReturnDate(loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now());
        Loan updatedLoan = loanRepository.save(existingLoan);
        game.setStatus(Game.GameStatus.AVAILABLE);
        gameRepository.save(game);
        return updatedLoan;
    }

    /**
     * Marca un préstamo como devuelto y lo devuelve como DTO.
     */
    public LoanResponseDTO returnLoanWithGameUpdateDTO(Loan loan) {
        return convertLoanToDTO(returnLoanWithGameUpdate(loan));
    }

    /**
     * Crea un préstamo a partir de un LoanDTO.
     */
    @Transactional
    public LoanResponseDTO createLoanFromDTO(LoanDTO loanDTO) {
        if (loanDTO.gameId() == null || loanDTO.borrowerId() == null)
            throw new BadRequestException("ID del juego y ID del prestatario son obligatorios.");
        Game game = gameService.findById(loanDTO.gameId());
        if (game.getStatus() != Game.GameStatus.AVAILABLE)
            throw new BadRequestException("El juego '" + game.getTitle() + "' no está disponible para préstamo.");
        User borrower = userService.getUserById(loanDTO.borrowerId());
        User lender = game.getUser();
        if (lender == null)
            throw new BadRequestException("El juego no tiene un propietario asignado y no puede ser prestado.");
        if (lender.getId().equals(borrower.getId()))
            throw new BadRequestException("El propietario del juego no puede tomar prestado su propio juego.");
        Loan loan = loanMapper.toEntity(loanDTO);
        loan.setGame(game);
        loan.setBorrower(borrower);
        loan.setLender(lender);
        if (loan.getLoanDate() == null) loan.setLoanDate(LocalDateTime.now());
        loan.setReturnDate(null);
        game.setStatus(Game.GameStatus.BORROWED);
        gameRepository.save(game);
        Loan savedLoan = loanRepository.save(loan);
        return convertLoanToDTO(savedLoan);
    }

    /**
     * Actualiza un préstamo desde un LoanDTO (solo notas y fecha esperada).
     */
    @Transactional
    public LoanResponseDTO updateLoanFromDTO(Long id, LoanDTO loanDTO) {
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));
        if (loanDTO.notes() != null) existingLoan.setNotes(loanDTO.notes());
        LocalDateTime expectedReturnDateFromDTO = loanDTO.expectedReturnDate();
        if (expectedReturnDateFromDTO != null) existingLoan.setExpectedReturnDate(expectedReturnDateFromDTO);
        Loan updatedLoan = loanRepository.save(existingLoan);
        return convertLoanToDTO(updatedLoan);
    }

    /**
     * Marca un préstamo como devuelto usando LoanReturnDTO y actualiza el estado del juego.
     */
    @Transactional
    public LoanResponseDTO recordLoanReturn(Long loanId, LoanReturnDTO returnDTO) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + loanId));
        if (loan.getReturnDate() != null)
            throw new BadRequestException("El préstamo ya ha sido devuelto.");
        LocalDateTime returnDate = returnDTO.returnDate();
        if (returnDate == null) returnDate = LocalDateTime.now();
        loan.setReturnDate(returnDate);
        Game game = loan.getGame();
        if (game != null) {
            game.setStatus(Game.GameStatus.AVAILABLE);
            gameRepository.save(game);
        }
        Loan savedLoan = loanRepository.save(loan);
        return convertLoanToDTO(savedLoan);
    }

    /**
     * Convierte una entidad Loan a LoanResponseDTO.
     */
    private LoanResponseDTO convertLoanToDTO(Loan loan) {
        if (loan == null) return null;
        Game gameEntity = loan.getGame();
        User borrowerEntity = loan.getBorrower();
        User lenderEntity = loan.getLender();
        GameSummaryDTO gameSummary = gameEntity != null ? new GameSummaryDTO(
                gameEntity.getId(), gameEntity.getTitle(), gameEntity.getPlatform(), gameEntity.getStatus()) : null;
        UserSummaryDTO borrowerSummary = borrowerEntity != null ? new UserSummaryDTO(
                borrowerEntity.getId(), borrowerEntity.getPublicName()) : null;
        UserSummaryDTO lenderSummary = lenderEntity != null ? new UserSummaryDTO(
                lenderEntity.getId(), lenderEntity.getPublicName()) : null;
        return new LoanResponseDTO(
                loan.getId(),
                loan.getLoanDate(),
                loan.getExpectedReturnDate(),
                loan.getReturnDate(),
                loan.getNotes(),
                gameSummary,
                lenderSummary,
                borrowerSummary
        );
    }
}
