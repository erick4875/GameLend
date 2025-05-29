package org.project.group5.gamelend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.GameMapper;
import org.project.group5.gamelend.repository.GameRepository;
import org.project.group5.gamelend.repository.LoanRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de juegos.
 * Maneja operaciones CRUD, búsquedas y validaciones de juegos.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GameService {

    // === Dependencias inyectadas ===
    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final UserService userService;
    private final DocumentService documentService;
    private final LoanRepository loanRepository;

    // === Operaciones de Búsqueda ===

    /**
     * Lista todos los juegos en formato entidad
     */
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        List<Game> games = gameRepository.findAll();
        log.debug("Se encontraron {} juegos.", games.size());
        return games;
    }

    /**
     * Lista todos los juegos en formato DTO
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findAllDTO() {
        List<Game> games = findAll();
        return gameMapper.toResponseDTOList(games);
    }

    /**
     * Busca juego por título
     * 
     * @throws BadRequestException       si el título es inválido
     * @throws ResourceNotFoundException si no existe
     */
    @Transactional(readOnly = true)
    public Game findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego no puede ser nulo o vacío.");
        }
        return gameRepository.findByTitleIgnoreCase(title)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con título: " + title));
    }

    @Transactional(readOnly = true)
    public GameResponseDTO findByIdDTO(Long id) {
        Game game = findById(id);
        return gameMapper.toResponseDTO(game);
    }

    @Transactional(readOnly = true)
    public GameResponseDTO findByTitleDTO(String title) {
        Game game = findByTitle(title);
        return gameMapper.toResponseDTO(game);
    }

    @Transactional(readOnly = true)
    public Game findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo.");
        }
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con ID: " + id));
    }

    /**
     * Guarda o actualiza un juego
     * 
     * @throws RuntimeException si hay error de base de datos
     */
    @Transactional
    public Game save(Game game) {
        try {
            Game savedGame = gameRepository.save(game);
            log.info("Juego '{}' guardado/actualizado con ID: {}", savedGame.getTitle(), savedGame.getId());
            return savedGame;
        } catch (DataAccessException e) {
            log.error("Error de BD al guardar/actualizar juego '{}': {}", game.getTitle(), e.getMessage(), e);
            throw new RuntimeException("Error de base de datos al guardar/actualizar el juego.", e);
        }
    }

    /**
     * Crea nuevo juego desde DTO
     * 
     * @throws BadRequestException si faltan datos requeridos
     */
    @Transactional
    public GameResponseDTO createGameFromDTO(GameDTO gameDTO) {
        log.info("Servicio: Creando nuevo juego con título: {}", gameDTO.title());
        if (gameDTO.userId() == null) {
            throw new BadRequestException("El ID del usuario es requerido para crear un juego.");
        }
        Game gameToCreate = gameMapper.toEntity(gameDTO);
        User owner = userService.getUserById(gameDTO.userId());
        gameToCreate.setUser(owner);
        if (gameDTO.imageId() != null) {
            Document imageDocument = documentService.find(gameDTO.imageId());
            gameToCreate.setImage(imageDocument);
        }
        if (gameDTO.catalogGameId() != null) {
            Game catalogGameRef = findById(gameDTO.catalogGameId());
            gameToCreate.setCatalogGame(catalogGameRef);
        }
        validateGame(gameToCreate);
        Game savedGame = gameRepository.save(gameToCreate);
        return gameMapper.toResponseDTO(savedGame);
    }

    /**
     * Actualiza juego existente desde DTO
     * 
     * @throws ResourceNotFoundException si no existe el juego
     */
    @Transactional
    public GameResponseDTO updateGameFromDTO(Long id, GameDTO gameDTO) {
        log.info("Servicio: Actualizando juego con ID: {} con datos: {}", id, gameDTO);
        Game gameToUpdate = findById(id);
        gameMapper.updateGameFromDto(gameDTO, gameToUpdate);
        Game updatedGame = gameRepository.save(gameToUpdate);
        return gameMapper.toResponseDTO(updatedGame);
    }

    // === Búsquedas por Usuario ===

    /**
     * Lista juegos con imágenes de un usuario
     * Incluye información de préstamos activos
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdWithImagesDTO(Long userId) {
        User user = userService.getUserById(userId);
        List<Game> games = gameRepository.findByUserIdAndImageIsNotNull(userId);
        return games.stream().map(game -> {
            Long activeLoanId = null;
            if (Game.GameStatus.BORROWED.equals(game.getStatus())) {
                List<Loan> activeLoans = loanRepository.findByGameAndLenderAndReturnDateIsNull(game, user);
                if (!activeLoans.isEmpty()) {
                    activeLoanId = activeLoans.get(0).getId();
                }
            }
            GameResponseDTO dto = gameMapper.toResponseDTO(game);
            return new GameResponseDTO(
                    dto.id(), dto.title(), dto.platform(), dto.genre(), dto.description(),
                    dto.status(), dto.userId(), dto.userName(), dto.imageId(), dto.imageUrl(),
                    dto.catalog(), dto.catalogGameId(),
                    activeLoanId);
        }).collect(Collectors.toList());
    }

    /**
     * Lista todos los juegos de un usuario
     * Incluye información de préstamos activos
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdDTO(Long userId) {
        User user = userService.getUserById(userId);
        List<Game> games = gameRepository.findByUserId(userId);

        return games.stream().map(game -> {
            Long activeLoanId = null;

            if (Game.GameStatus.BORROWED.equals(game.getStatus())) {
                List<Loan> activeLoans = loanRepository.findByGameAndLenderAndReturnDateIsNull(game, user);
                if (!activeLoans.isEmpty()) {
                    activeLoanId = activeLoans.get(0).getId();
                    log.debug("Juego ID {} del usuario {} está prestado, activeLoanId: {}", game.getId(), userId,
                            activeLoanId);
                } else {
                    log.debug(
                            "Juego ID {} del usuario {} está BORROWED pero no se encontró préstamo activo donde sea lender.",
                            game.getId(), userId);
                }
            }

            // Usar el GameMapper para los campos base
            GameResponseDTO dto = gameMapper.toResponseDTO(game);

            // Devolver un nuevo DTO con el activeLoanId
            return new GameResponseDTO(
                    dto.id(), dto.title(), dto.platform(), dto.genre(), dto.description(),
                    dto.status(), dto.userId(), dto.userName(), dto.imageId(), dto.imageUrl(),
                    dto.catalog(), dto.catalogGameId(),
                    activeLoanId);
        }).collect(Collectors.toList());
    }

    /**
     * Lista todos los juegos de un usuario
     */
    @Transactional(readOnly = true)
    public List<Game> findByUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }
        List<Game> games = gameRepository.findByUserId(userId);
        log.info("Se encontraron {} juegos para el usuario {}", games.size(), userId);
        return games;
    }

    /**
     * Elimina un juego por su ID
     * 
     * @param id ID del juego a eliminar
     * @throws BadRequestException       si el ID es nulo
     * @throws ResourceNotFoundException si el juego no existe
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Eliminando juego con ID: {}", id);

        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo");
        }

        // Verifica si el juego existe
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con ID: " + id));

        // Verifica si el juego puede ser eliminado (no está prestado)
        if (Game.GameStatus.BORROWED.equals(game.getStatus())) {
            throw new BadRequestException("No se puede eliminar un juego que está prestado");
        }

        try {
            gameRepository.deleteById(id);
            log.info("Juego eliminado correctamente: {}", id);
        } catch (Exception e) {
            log.error("Error al eliminar juego con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar el juego", e);
        }
    }

    // === Validaciones ===

    /**
     * Valida los campos obligatorios de un juego
     * - Título no vacío
     * - Plataforma no vacía
     * - Género no vacío
     * - Usuario propietario asignado
     * - Estado válido (AVAILABLE por defecto)
     * 
     * @throws BadRequestException si algún campo requerido es inválido
     */
    private void validateGame(Game game) {
        if (game == null) {
            throw new BadRequestException("El juego no puede ser nulo");
        }

        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            throw new BadRequestException("El título del juego es obligatorio");
        }

        if (game.getPlatform() == null || game.getPlatform().trim().isEmpty()) {
            throw new BadRequestException("La plataforma del juego es obligatoria");
        }

        if (game.getGenre() == null || game.getGenre().trim().isEmpty()) {
            throw new BadRequestException("El género del juego es obligatorio");
        }

        if (game.getUser() == null) {
            throw new BadRequestException("El propietario del juego es obligatorio");
        }

        if (game.getStatus() == null) {
            game.setStatus(Game.GameStatus.AVAILABLE);
        }

        log.debug("Validación exitosa para juego: {}", game.getTitle());
    }
}
