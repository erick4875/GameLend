package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.LoanDTO;
import org.project.group5.gamelend.dto.LoanRequestDTO;
import org.project.group5.gamelend.dto.LoanResponseDTO;
import org.project.group5.gamelend.dto.LoanReturnDTO;
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
    private final UserService userService;
    private final GameService gameService;
    private final UserMapper userMapper;
    private final LoanMapper loanMapper;

    public Loan saveLoan(Loan loan) {
        if (loan == null)
            throw new BadRequestException("El préstamo no puede ser nulo");
        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Loan getLoanById(Long id) {
        if (id == null)
            throw new BadRequestException("El ID del préstamo no puede ser nulo");
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));
    }

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

    @Transactional
    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan existingLoan = getLoanById(id);
        existingLoan.setLoanDate(loanDetails.getLoanDate());
        existingLoan.setExpectedReturnDate(loanDetails.getExpectedReturnDate());
        existingLoan.setReturnDate(loanDetails.getReturnDate());
        existingLoan.setNotes(loanDetails.getNotes());
        return loanRepository.save(existingLoan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getAllLoansDTO() {
        return loanRepository.findAll().stream()
                .map(this::convertLoanToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponseDTO createLoan(LoanRequestDTO request) {
        if (request == null || request.gameId() == null) {
            throw new BadRequestException("El ID del juego es obligatorio");
        }

        User borrower = getCurrentAuthenticatedUser();
        Game game = gameService.findById(request.gameId());

        validateGameAvailability(game);
        User lender = validateAndGetLender(game, borrower);
        validateNoActiveLoans(game, borrower);

        Loan loan = loanMapper.toEntity(request);
        loan.setLender(lender);
        loan.setBorrower(borrower);

        game.setStatus(Game.GameStatus.BORROWED);
        gameRepository.save(game);

        return convertLoanToResponseDTO(loanRepository.save(loan));
    }

    private LoanResponseDTO convertLoanToResponseDTO(Loan loan) {
        if (loan == null)
            return null;

        GameSummaryDTO gameSummary = loan.getGame() != null ? new GameSummaryDTO(
                loan.getGame().getId(),
                loan.getGame().getTitle(),
                loan.getGame().getPlatform(),
                loan.getGame().getStatus()) : null;

        UserSummaryDTO lenderSummary = loan.getLender() != null ? userMapper.toUserSummaryDTO(loan.getLender()) : null;

        UserSummaryDTO borrowerSummary = loan.getBorrower() != null ? userMapper.toUserSummaryDTO(loan.getBorrower())
                : null;

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

    // Este método createLoanFromGenericDTO usa un LoanDTO más general.
    // Mantenlo si tienes un caso de uso para él (ej. un admin creando un préstamo
    // con todos los detalles).
    @Transactional
    public LoanResponseDTO createLoanFromGenericDTO(LoanDTO loanDTO) {
        log.info("Creando préstamo desde LoanDTO genérico: {}", loanDTO);
        if (loanDTO.gameId() == null || loanDTO.borrowerId() == null || loanDTO.lenderId() == null) {
            throw new BadRequestException("IDs de juego, prestatario y prestamista son obligatorios para este DTO.");
        }
        Game game = gameService.findById(loanDTO.gameId());
        if (game.getStatus() != Game.GameStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El juego '" + game.getTitle() + "' no está disponible para préstamo.");
        }
        User borrower = userService.getUserById(loanDTO.borrowerId());
        User lender = userService.getUserById(loanDTO.lenderId());

        if (lender.getId().equals(borrower.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El propietario del juego no puede tomar prestado su propio juego.");
        }

        Loan loan = loanMapper.toEntity(loanDTO);
        loan.setGame(game);
        loan.setBorrower(borrower);
        loan.setLender(lender);
        if (loan.getLoanDate() == null)
            loan.setLoanDate(LocalDateTime.now());
        if (loan.getExpectedReturnDate() == null)
            loan.setExpectedReturnDate(LocalDateTime.now().plusWeeks(2));
        loan.setReturnDate(null);

        game.setStatus(Game.GameStatus.BORROWED);
        gameRepository.save(game);

        Loan savedLoan = loanRepository.save(loan);
        return convertLoanToResponseDTO(savedLoan);
    }

    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanDTO(Long id) {
        return convertLoanToResponseDTO(getLoanById(id));
    }

    @Transactional
    public LoanResponseDTO updateLoanFromDTO(Long id, LoanDTO loanDTO) {
        Loan existingLoan = getLoanById(id);
        // Actualizar solo los campos permitidos
        if (loanDTO.notes() != null) {
            existingLoan.setNotes(loanDTO.notes());
        }
        if (loanDTO.expectedReturnDate() != null) {
            existingLoan.setExpectedReturnDate(loanDTO.expectedReturnDate());
        }
        return convertLoanToResponseDTO(loanRepository.save(existingLoan));
    }

    /**
     * Registra la devolución de un préstamo.
     *
     * @param id ID del préstamo a devolver
     * @param returnDTO DTO con los datos de la devolución
     * @return DTO con los datos actualizados del préstamo
     * @throws BadRequestException si el préstamo ya fue devuelto
     * @throws ResourceNotFoundException si el préstamo no existe
     */
    @Transactional
    public LoanResponseDTO recordLoanReturn(Long id, LoanReturnDTO returnDTO) {
        log.debug("Registrando devolución para préstamo ID: {}", id);
        
        Loan loan = getLoanById(id);
        
        if (loan.getReturnDate() != null) {
            throw new BadRequestException("El préstamo ya ha sido devuelto");
        }

        // Usar la fecha proporcionada o la actual si no se proporciona
        loan.setReturnDate(returnDTO.returnDate() != null ? 
            returnDTO.returnDate() : LocalDateTime.now());

        // Actualizar estado del juego
        Game game = loan.getGame();
        if (game != null) {
            game.setStatus(Game.GameStatus.AVAILABLE);
            gameRepository.save(game);
            log.debug("Estado del juego actualizado a AVAILABLE");
        }

        Loan savedLoan = loanRepository.save(loan);
        log.info("Devolución registrada para préstamo ID: {}", id);
        
        return convertLoanToResponseDTO(savedLoan);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No hay usuario autenticado");
        }

        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userService.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Valida si un juego está disponible para préstamo.
     * 
     * @param game El juego a validar
     * @throws ResponseStatusException si el juego no está disponible
     */
    private void validateGameAvailability(Game game) {
        if (game == null) {
            throw new BadRequestException("El juego no puede ser nulo");
        }

        if (!Game.GameStatus.AVAILABLE.equals(game.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("El juego '%s' no está disponible para préstamo", game.getTitle()));
        }

        log.debug("Juego {} validado como disponible", game.getId());
    }

    /**
     * Valida y obtiene el prestador del juego.
     * 
     * @param game     El juego a prestar
     * @param borrower El usuario que solicita el préstamo
     * @return El usuario prestador (propietario del juego)
     * @throws BadRequestException si el juego no tiene propietario
     * @throws ForbiddenException  si el prestador es el mismo que el prestatario
     */
    private User validateAndGetLender(Game game, User borrower) {
        User lender = game.getUser();
        if (lender == null) {
            throw new BadRequestException("El juego no tiene un propietario asignado");
        }

        if (lender.getId().equals(borrower.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes solicitar un préstamo de tu propio juego");
        }

        log.debug("Prestador validado: {}", lender.getId());
        return lender;
    }

    /**
     * Valida que no existan préstamos activos del mismo juego para el usuario.
     * 
     * @param game     El juego a validar
     * @param borrower El usuario prestatario
     * @throws ResponseStatusException si ya existe un préstamo activo
     */
    private void validateNoActiveLoans(Game game, User borrower) {
        boolean existingActiveLoan = loanRepository
                .existsByGameAndBorrowerAndReturnDateIsNull(game, borrower);

        if (existingActiveLoan) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya tienes un préstamo activo para este juego");
        }

        log.debug("No se encontraron préstamos activos para el juego {} y usuario {}",
                game.getId(), borrower.getId());
    }
}
