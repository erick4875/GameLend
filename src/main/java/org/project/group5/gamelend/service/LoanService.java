package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * Maneja la lógica de negocio relacionada con la creación,
 * actualización, eliminación y consulta de préstamos.
 */
@Service
@RequiredArgsConstructor // Inyección de dependencias por constructor de Lombok
@Slf4j // Logger de Lombok
public class LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;
    private final LoanMapper loanMapper; // Mapper para convertir entre entidad Loan y DTOs
    private final UserService userService; // Servicio de usuarios para obtener datos del prestatario/prestamista
    private final GameService gameService; // Servicio de juegos para obtener datos del juego

    // Formateador estándar para fechas en DTOs
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Crea un nuevo préstamo directamente desde una entidad Loan.
     * Usado internamente o en escenarios donde la entidad ya está construida.
     * 
     * @param loan Objeto préstamo a guardar.
     * @return Préstamo guardado.
     * @throws BadRequestException si el préstamo es nulo.
     * @throws RuntimeException si ocurre un error durante el guardado.
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
            // Considerar una excepción más específica si es posible
            throw new RuntimeException("Error al guardar el préstamo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los préstamos registrados.
     * 
     * @return Lista de todas las entidades Loan.
     */
    public List<Loan> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos en la base de datos.");
        }
        return loans;
    }

    /**
     * Obtiene una entidad Loan por su ID.
     * 
     * @param id ID del préstamo.
     * @return Entidad Loan encontrada.
     * @throws BadRequestException si el ID es nulo.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    public Loan getLoanById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del préstamo no puede ser nulo");
        }

        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Préstamo no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Préstamo no encontrado con ID: " + id);
                });
    }

    /**
     * Elimina un préstamo por su ID.
     * Si el juego asociado estaba prestado únicamente por este préstamo,
     * actualiza el estado del juego a DISPONIBLE.
     * 
     * @param id ID del préstamo a eliminar.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id); // Usa el método existente para obtener y manejar ResourceNotFoundException

        Game game = loan.getGame();
        // Si el juego existe y estaba marcado como PRESTADO
        if (game != null && game.getStatus() == Game.GameStatus.BORROWED) {
            // Verifica si hay OTROS préstamos activos para el mismo juego
            boolean isStillLentOutByAnotherLoan = loanRepository.findByGameIdAndReturnDateIsNull(game.getId())
                    .stream()
                    .anyMatch(otherLoan -> !otherLoan.getId().equals(id)); // Excluye el préstamo actual

            // Si no hay otros préstamos activos para este juego, marcarlo como DISPONIBLE
            if (!isStillLentOutByAnotherLoan) {
                game.setStatus(Game.GameStatus.AVAILABLE);
                gameRepository.save(game);
                log.info("Juego {} marcado como DISPONIBLE tras eliminar préstamo {}", game.getId(), id);
            }
        }
        loanRepository.delete(loan);
        log.info("Préstamo con ID {} eliminado correctamente.", id);
    }

    /**
     * Actualiza un préstamo existente con datos de otra entidad Loan.
     * 
     * @param id   ID del préstamo a actualizar.
     * @param loan Nuevos datos del préstamo.
     * @return Préstamo actualizado.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    public Loan updateLoan(Long id, Loan loan) {
        // getLoanById ya maneja la ResourceNotFoundException si no existe
        Loan existingLoan = getLoanById(id);

        // Actualiza los campos de la entidad existente
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
     * Registra la devolución de un préstamo (versión simplificada, solo actualiza fecha).
     * Considerar usar `returnLoanWithGameUpdate` para una lógica más completa.
     * 
     * @param loan El préstamo con la fecha de devolución actualizada.
     * @return Préstamo actualizado.
     * @throws BadRequestException si la fecha de devolución es nula.
     */
    public Loan returnLoan(Loan loan) {
        if (loan.getReturnDate() == null) {
            throw new BadRequestException("Fecha de devolución no puede ser nula");
        }
        Loan existingLoan = getLoanById(loan.getId()); // Valida existencia
        existingLoan.setReturnDate(loan.getReturnDate());
        return loanRepository.save(existingLoan);
    }

    /**
     * Obtiene todos los préstamos y los convierte a DTOs (LoanResponseDTO).
     * 
     * @return Lista de DTOs de préstamos.
     */
    public List<LoanResponseDTO> getAllLoansDTO() {
        List<Loan> loans = loanRepository.findAll();
        if (loans.isEmpty()) {
            log.info("No se encontraron préstamos para convertir a DTO.");
            return List.of(); // Devuelve lista vacía en lugar de null
        }
        return loans.stream()
                .map(this::convertLoanToDTO) // Reutiliza el método de conversión
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un préstamo por su ID y lo convierte a LoanResponseDTO.
     * 
     * @param id ID del préstamo.
     * @return DTO del préstamo.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    public LoanResponseDTO getLoanByIdDTO(Long id) {
        Loan loan = getLoanById(id); // Reutiliza la búsqueda y manejo de excepciones
        return convertLoanToDTO(loan);
    }

    /**
     * Guarda un préstamo (entidad) y devuelve su representación como LoanResponseDTO.
     * 
     * @param loan Préstamo a guardar.
     * @return DTO del préstamo guardado.
     */
    public LoanResponseDTO saveLoanDTO(Loan loan) {
        Loan savedLoan = saveLoan(loan); // Guarda la entidad primero
        return convertLoanToDTO(savedLoan); // Luego convierte a DTO
    }

    /**
     * Actualiza un préstamo (entidad) y devuelve su representación como LoanResponseDTO.
     * 
     * @param id   ID del préstamo a actualizar.
     * @param loan Nuevos datos del préstamo (entidad).
     * @return DTO del préstamo actualizado.
     */
    public LoanResponseDTO updateLoanDTO(Long id, Loan loan) {
        Loan updatedLoan = updateLoan(id, loan); // Actualiza la entidad
        return convertLoanToDTO(updatedLoan); // Convierte a DTO
    }

    /**
     * Registra la devolución de un préstamo y actualiza el estado del juego a DISPONIBLE.
     * 
     * @param loan El préstamo con la fecha de devolución actualizada.
     * @return Entidad Loan actualizada.
     * @throws BadRequestException si el préstamo es nulo, no tiene ID, ya fue devuelto o no tiene juego asociado.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    @Transactional
    public Loan returnLoanWithGameUpdate(Loan loan) {
        if (loan == null || loan.getId() == null) {
            throw new BadRequestException("El préstamo no puede ser nulo y debe tener un ID válido");
        }

        Loan existingLoan = loanRepository.findById(loan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo con ID " + loan.getId() + " no existe"));

        if (existingLoan.getReturnDate() != null) {
            log.warn("El préstamo con ID {} ya ha sido devuelto anteriormente.", loan.getId());
            throw new BadRequestException("El préstamo ya ha sido devuelto");
        }

        Game game = existingLoan.getGame();
        if (game == null) {
            // Esto no debería ocurrir si la integridad de datos es correcta
            log.error("El préstamo ID {} no tiene un juego asociado. No se puede actualizar estado del juego.", loan.getId());
            throw new BadRequestException("El préstamo no tiene un juego asociado");
        }

        // Asigna la fecha de devolución del objeto 'loan' pasado como parámetro
        existingLoan.setReturnDate(loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now());
        Loan updatedLoan = loanRepository.save(existingLoan);

        // Actualiza el estado del juego a DISPONIBLE
        game.setStatus(Game.GameStatus.AVAILABLE);
        gameRepository.save(game);

        log.info("Préstamo con ID {} devuelto correctamente. Juego ID {} marcado como DISPONIBLE.", updatedLoan.getId(), game.getId());
        return updatedLoan;
    }

    /**
     * Registra la devolución de un préstamo (usando la entidad) y devuelve su LoanResponseDTO.
     * 
     * @param loan El préstamo con la fecha de devolución actualizada.
     * @return DTO del préstamo actualizado.
     */
    public LoanResponseDTO returnLoanWithGameUpdateDTO(Loan loan) {
        Loan returnedLoan = returnLoanWithGameUpdate(loan);
        return convertLoanToDTO(returnedLoan);
    }

    /**
     * Crea un nuevo préstamo a partir de un LoanDTO.
     * Maneja la lógica de validación, asignación de entidades y actualización del estado del juego.
     * 
     * @param loanDTO DTO con la información para crear el préstamo.
     * @return LoanResponseDTO del préstamo creado.
     * @throws BadRequestException si faltan datos, el juego no está disponible o el prestamista es el mismo que el prestatario.
     */
    @Transactional
    public LoanResponseDTO createLoanFromDTO(LoanDTO loanDTO) {
        if (loanDTO.gameId() == null || loanDTO.borrowerId() == null) {
            throw new BadRequestException("ID del juego y ID del prestatario son obligatorios.");
        }

        // Obtener la entidad Game
        Game game = gameService.findById(loanDTO.gameId());
        if (game.getStatus() != Game.GameStatus.AVAILABLE) {
            log.warn("Intento de prestar juego no disponible: ID {}, Título '{}', Estado: {}", game.getId(), game.getTitle(), game.getStatus());
            throw new BadRequestException("El juego '" + game.getTitle() + "' no está disponible para préstamo.");
        }

        // Obtener la entidad User para el prestatario (borrower)
        User borrower = userService.getUserById(loanDTO.borrowerId());
        // El prestamista (lender) es el propietario del juego
        User lender = game.getUser();

        if (lender == null) {
            // Esto indicaría un juego de catálogo o un juego sin propietario asignado, no debería ser prestable por un usuario.
            log.error("Intento de crear préstamo para juego ID {} que no tiene propietario (lender).", game.getId());
            throw new BadRequestException("El juego no tiene un propietario asignado y no puede ser prestado de esta forma.");
        }

        if (lender.getId().equals(borrower.getId())) {
            log.warn("Intento de autopréstamo: Usuario ID {} para juego ID {}", lender.getId(), game.getId());
            throw new BadRequestException("El propietario del juego no puede tomar prestado su propio juego.");
        }

        // Convertir DTO a entidad Loan
        Loan loan = loanMapper.toEntity(loanDTO);
        loan.setGame(game);
        loan.setBorrower(borrower);
        loan.setLender(lender); // Asignar el propietario del juego como prestamista

        // Establecer fechas si no vienen en el DTO
        if (loan.getLoanDate() == null) {
            loan.setLoanDate(LocalDateTime.now());
        }
        // La fecha de devolución es nula al crear el préstamo
        loan.setReturnDate(null);
        // La fecha esperada de devolución viene del DTO (a través del mapper)

        // Actualizar estado del juego a PRESTADO
        game.setStatus(Game.GameStatus.BORROWED);
        gameRepository.save(game);

        // Guardar el préstamo
        Loan savedLoan = loanRepository.save(loan);
        log.info("Préstamo creado a través de DTO con ID: {}. Juego ID: {}, Prestamista ID: {}, Prestatario ID: {}",
                savedLoan.getId(), game.getId(), lender.getId(), borrower.getId());
        return convertLoanToDTO(savedLoan);
    }

    /**
     * Actualiza un préstamo existente a partir de un LoanDTO.
     * Solo permite actualizar notas y fecha esperada de devolución.
     * 
     * @param id      ID del préstamo a actualizar.
     * @param loanDTO DTO con los datos a actualizar.
     * @return LoanResponseDTO del préstamo actualizado.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     */
    @Transactional
    public LoanResponseDTO updateLoanFromDTO(Long id, LoanDTO loanDTO) {
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));

        // Actualizar solo los campos permitidos desde el DTO
        if (loanDTO.notes() != null) {
            existingLoan.setNotes(loanDTO.notes());
        }
        // El LoanDTO tiene un método para convertir la fecha esperada de String a LocalDateTime
        LocalDateTime expectedReturnDateFromDTO = loanDTO.getExpectedReturnDateAsDateTime();
        if (expectedReturnDateFromDTO != null) {
            existingLoan.setExpectedReturnDate(expectedReturnDateFromDTO);
        }

        Loan updatedLoan = loanRepository.save(existingLoan);
        log.info("Préstamo con ID {} actualizado desde DTO.", updatedLoan.getId());
        return convertLoanToDTO(updatedLoan);
    }

    /**
     * Registra la devolución de un préstamo utilizando su ID y un LoanReturnDTO.
     * Actualiza el estado del juego a DISPONIBLE.
     * 
     * @param loanId    ID del préstamo a devolver.
     * @param returnDTO DTO con la fecha de devolución.
     * @return LoanResponseDTO del préstamo devuelto.
     * @throws ResourceNotFoundException si el préstamo no se encuentra.
     * @throws BadRequestException si el préstamo ya fue devuelto.
     */
    @Transactional
    public LoanResponseDTO recordLoanReturn(Long loanId, LoanReturnDTO returnDTO) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + loanId));

        if (loan.getReturnDate() != null) {
            log.warn("Intento de devolver préstamo ID {} que ya fue devuelto el {}", loanId, loan.getReturnDate());
            throw new BadRequestException("El préstamo ya ha sido devuelto.");
        }

        // Obtener la fecha de devolución del DTO. Si es nula o inválida, usar la fecha actual.
        LocalDateTime returnDate = returnDTO.getReturnDateAsDateTime();
        if (returnDate == null) {
            log.warn("Fecha de devolución inválida o nula en DTO para préstamo ID {}. Usando fecha y hora actual.", loanId);
            returnDate = LocalDateTime.now();
        }
        loan.setReturnDate(returnDate);

        Game game = loan.getGame();
        if (game != null) {
            game.setStatus(Game.GameStatus.AVAILABLE);
            gameRepository.save(game);
            log.info("Juego ID {} asociado al préstamo ID {} marcado como DISPONIBLE.", game.getId(), loanId);
        } else {
            // Esto es un caso anómalo, un préstamo debería tener siempre un juego.
            log.warn("El préstamo con ID {} no tiene un juego asociado. No se pudo actualizar el estado del juego.", loanId);
        }

        Loan savedLoan = loanRepository.save(loan);
        log.info("Devolución registrada para préstamo ID {}. Fecha de devolución: {}", savedLoan.getId(), savedLoan.getReturnDate());
        return convertLoanToDTO(savedLoan);
    }

    /**
     * Método helper para convertir una entidad Loan a LoanResponseDTO.
     * 
     * @param loan Entidad Loan a convertir.
     * @return LoanResponseDTO correspondiente, o null si la entrada es null.
     */
    private LoanResponseDTO convertLoanToDTO(Loan loan) {
        if (loan == null) {
            return null;
        }

        Game gameEntity = loan.getGame();
        User borrowerEntity = loan.getBorrower();
        User lenderEntity = loan.getLender();

        // Crear DTOs resumen para juego, prestatario y prestamista
        GameSummaryDTO gameSummary = null;
        if (gameEntity != null) {
            gameSummary = new GameSummaryDTO(
                    gameEntity.getId(),
                    gameEntity.getTitle(),
                    gameEntity.getPlatform(),
                    gameEntity.getStatus() // El estado actual del juego
            );
        }

        UserSummaryDTO borrowerSummary = null;
        if (borrowerEntity != null) {
            borrowerSummary = new UserSummaryDTO(
                    borrowerEntity.getId(),
                    borrowerEntity.getPublicName() // Usar nombre público para el DTO
            );
        }

        UserSummaryDTO lenderSummary = null;
        if (lenderEntity != null) {
            lenderSummary = new UserSummaryDTO(
                    lenderEntity.getId(),
                    lenderEntity.getPublicName() // Usar nombre público para el DTO
            );
        }

        // Formatear fechas a String para el DTO
        String loanDateStr = loan.getLoanDate() != null ? loan.getLoanDate().format(DATE_FORMATTER) : null;
        String expectedReturnDateStr = loan.getExpectedReturnDate() != null
                ? loan.getExpectedReturnDate().format(DATE_FORMATTER)
                : null;
        String returnDateStr = loan.getReturnDate() != null ? loan.getReturnDate().format(DATE_FORMATTER) : null;

        // Construir y devolver el LoanResponseDTO
        // Esta llamada al constructor debe coincidir con la definición del record LoanResponseDTO (8 argumentos)
        return new LoanResponseDTO(
                loan.getId(),
                loanDateStr,
                expectedReturnDateStr,
                returnDateStr,
                loan.getNotes(),
                gameSummary,
                lenderSummary,
                borrowerSummary
        );
    }
}
