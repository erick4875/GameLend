package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanDTO; // DTO genérico
import org.project.group5.gamelend.dto.LoanRequestDTO; // Para nuevas solicitudes (contiene gameId y opcionalmente notes)
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO; // Para registrar devoluciones (contiene returnDate)
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.LoanMapper; // Si lo usas para LoanDTO
import org.project.group5.gamelend.mapper.UserMapper; // Para convertir User a UserSummaryDTO
import org.project.group5.gamelend.repository.GameRepository;
import org.project.group5.gamelend.repository.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;
    private final LoanMapper loanMapper;
    private final UserService userService;
    private final GameService gameService;
    private final UserMapper userMapper;

    /**
     * Guarda una entidad Loan.
     */
    @Transactional
    public Loan saveLoan(Loan loan) {
        if (loan == null)
            throw new BadRequestException("El préstamo no puede ser nulo");
        return loanRepository.save(loan);
    }

    /**
     * Obtiene todas las entidades Loan.
     */
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Obtiene una entidad Loan por ID.
     * Este es el método principal para obtener la entidad.
     */
    @Transactional(readOnly = true)
    public Loan getLoanById(Long id) {
        if (id == null)
            throw new BadRequestException("El ID del préstamo no puede ser nulo");
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
            boolean otherActiveLoanForThisGame = loanRepository.findByGameIdAndReturnDateIsNull(game.getId())
                    .stream()
                    .anyMatch(l -> !l.getId().equals(id));
            if (!otherActiveLoanForThisGame) {
                game.setStatus(Game.GameStatus.AVAILABLE);
                gameRepository.save(game);
                log.info("Juego ID {} actualizado a AVAILABLE después de eliminar préstamo ID {}", game.getId(), id);
            }
        }
        loanRepository.delete(loan);
        log.info("Préstamo ID {} eliminado.", id);
    }

    /**
     * Actualiza una entidad Loan existente con datos de otra entidad Loan.
     */
    @Transactional
    public Loan updateLoanEntity(Long id, Loan loanDetails) {
        Loan existingLoan = getLoanById(id);
        existingLoan.setLoanDate(loanDetails.getLoanDate());
        existingLoan.setExpectedReturnDate(loanDetails.getExpectedReturnDate());
        existingLoan.setReturnDate(loanDetails.getReturnDate());
        existingLoan.setNotes(loanDetails.getNotes());
        return loanRepository.save(existingLoan);
    }

    /**
     * Obtiene todos los préstamos como una lista de LoanResponseDTO.
     */
    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getAllLoansDTO() {
        return loanRepository.findAll().stream()
                .map(this::convertLoanToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un préstamo por ID como LoanResponseDTO.
     * Este es el método que el controlador debería usar para obtener un DTO.
     */
    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanByIdAsResponseDTO(Long id) { // Renombrado para claridad
        return convertLoanToResponseDTO(getLoanById(id)); // Llama al método getLoanById de esta clase
    }

    /**
     * Crea una nueva solicitud de préstamo.
     */
    @Transactional
    public LoanResponseDTO createLoanRequest(LoanRequestDTO loanRequestDto) {
        if (loanRequestDto == null || loanRequestDto.gameId() == null) {
            throw new BadRequestException("El ID del juego es obligatorio");
        }
        log.info("LoanService: Creando préstamo para gameId: {}", loanRequestDto.gameId());

        User borrower = getCurrentAuthenticatedUser();
        Game game = gameService.findById(loanRequestDto.gameId());

        validateGameAvailability(game);
        User lender = validateAndGetLender(game, borrower);
        validateNoActiveLoans(game, borrower);

        Loan newLoan = Loan.builder()
                .game(game)
                .lender(lender)
                .borrower(borrower)
                .loanDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusWeeks(2))
                .notes(loanRequestDto.notes())
                .build();

        game.setStatus(Game.GameStatus.BORROWED);
        gameRepository.save(game);

        Loan savedLoan = loanRepository.save(newLoan);
        log.info("Nueva solicitud de préstamo guardada con ID: {}", savedLoan.getId());
        return convertLoanToResponseDTO(savedLoan);
    }

    /**
     * Actualiza un préstamo desde un LoanDTO genérico (principalmente notas y fecha
     * esperada de devolución).
     */
    @Transactional
    public LoanResponseDTO updateLoanFromDTO(Long id, LoanDTO loanDTO) {
        Loan existingLoan = getLoanById(id);
        boolean changed = false;
        if (loanDTO.notes() != null && !loanDTO.notes().equals(existingLoan.getNotes())) {
            existingLoan.setNotes(loanDTO.notes());
            changed = true;
        }
        if (loanDTO.expectedReturnDate() != null
                && !loanDTO.expectedReturnDate().equals(existingLoan.getExpectedReturnDate())) {
            existingLoan.setExpectedReturnDate(loanDTO.expectedReturnDate());
            changed = true;
        }
        Loan updatedLoan = changed ? loanRepository.save(existingLoan) : existingLoan;
        return convertLoanToResponseDTO(updatedLoan);
    }

    /**
     * Registra la devolución de un préstamo usando LoanReturnDTO y actualiza el
     * estado del juego.
     */
    @Transactional
    public LoanResponseDTO recordLoanReturn(Long loanId, LoanReturnDTO returnDTO) {
        Loan loan = getLoanById(loanId);
        if (loan.getReturnDate() != null) {
            throw new BadRequestException("El préstamo ya ha sido devuelto.");
        }
        LocalDateTime returnDate = returnDTO.returnDate();
        if (returnDate == null)
            returnDate = LocalDateTime.now();
        loan.setReturnDate(returnDate);

        Game game = loan.getGame();
        if (game != null) {
            boolean otherActiveLoansForGame = loanRepository.findByGameIdAndReturnDateIsNull(game.getId())
                    .stream()
                    .anyMatch(l -> !l.getId().equals(loanId));
            if (!otherActiveLoansForGame) {
                game.setStatus(Game.GameStatus.AVAILABLE);
                gameRepository.save(game);
            }
        }
        Loan savedLoan = loanRepository.save(loan);
        return convertLoanToResponseDTO(savedLoan);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo determinar el nombre de usuario del principal.");
        }
        return userService.findUserByEmail(username);
    }

    private LoanResponseDTO convertLoanToResponseDTO(Loan loan) {
        if (loan == null)
            return null;
        Game gameEntity = loan.getGame();
        User borrowerEntity = loan.getBorrower();
        User lenderEntity = loan.getLender();

        UserSummaryDTO borrowerSummary = (borrowerEntity != null) ? userMapper.toUserSummaryDTO(borrowerEntity) : null;
        UserSummaryDTO lenderSummary = (lenderEntity != null) ? userMapper.toUserSummaryDTO(lenderEntity) : null;

        GameSummaryDTO gameSummary = null;
        if (gameEntity != null) {
            gameSummary = new GameSummaryDTO(
                    gameEntity.getId(),
                    gameEntity.getTitle(),
                    gameEntity.getPlatform(),
                    gameEntity.getStatus());
        }

        return new LoanResponseDTO(
                loan.getId(),
                loan.getLoanDate(),
                loan.getExpectedReturnDate(),
                loan.getReturnDate(),
                loan.getNotes(),
                gameSummary,
                lenderSummary,
                borrowerSummary);
    }

    // --- Métodos de Validación ---
    private void validateGameAvailability(Game game) {
        if (!Game.GameStatus.AVAILABLE.equals(game.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El juego '" + game.getTitle() + "' no está disponible para préstamo.");
        }
    }

    private User validateAndGetLender(Game game, User borrower) {
        User lender = game.getUser();
        if (lender == null) {
            throw new BadRequestException("El juego no tiene un propietario asignado.");
        }
        if (lender.getId().equals(borrower.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes solicitar un préstamo de tu propio juego.");
        }
        return lender;
    }

    private void validateNoActiveLoans(Game game, User borrower) {
        boolean existingActiveLoan = loanRepository.existsByGameAndBorrowerAndReturnDateIsNull(game, borrower);
        if (existingActiveLoan) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya tienes un préstamo activo o una solicitud pendiente para este juego.");
        }
    }
}