package org.project.group5.gamelend.service;

import java.util.List;

import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.GameMapper;
import org.project.group5.gamelend.repository.GameRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de juegos en la aplicación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;

    /**
     * Obtiene todos los juegos registrados
     * @return Lista de juegos
     */
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        List<Game> games = gameRepository.findAll();
        if (games.isEmpty()) {
            log.info("No se encontraron juegos en la base de datos");
        } else {
            log.debug("Se encontraron {} juegos", games.size());
        }
        return games;
    }

    /**
     * Encuentra todos los juegos y los convierte a DTOs
     * @return Lista de GameResponseDTO
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findAllDTO() {
        List<Game> games = findAll();
        return gameMapper.toResponseDTOList(games);
    }

    /**
     * Busca un juego por su título
     * @param title Título del juego a buscar
     * @return Juego encontrado
     * @throws BadRequestException si el título es nulo o vacío
     * @throws ResourceNotFoundException si el juego no existe
     */
    @Transactional(readOnly = true)
    public Game findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego no puede ser nulo o vacío");
        }

        return gameRepository.findByTitleIgnoreCase(title)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con título: " + title));
    }

    /**
     * Busca un juego por su ID y lo convierte a DTO
     * @param id ID del juego a buscar
     * @return DTO del juego encontrado
     */
    @Transactional(readOnly = true)
    public GameResponseDTO findByIdDTO(Long id) {
        Game game = findById(id);
        return gameMapper.toResponseDTO(game);
    }

    /**
     * Busca un juego por su título y lo convierte a DTO
     * @param title Título del juego a buscar
     * @return DTO del juego encontrado
     */
    @Transactional(readOnly = true)
    public GameResponseDTO findByTitleDTO(String title) {
        Game game = findByTitle(title);
        return gameMapper.toResponseDTO(game);
    }

    /**
     * Busca un juego por su ID
     * @param id ID del juego a buscar
     * @return Juego encontrado
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si el juego no existe
     */
    @Transactional(readOnly = true)
    public Game findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo");
        }

        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo juego en la base de datos
     * @param game Juego a guardar
     * @return Juego guardado
     * @throws BadRequestException si el juego es nulo o tiene datos inválidos
     */
    @Transactional
    public Game save(Game game) {
        validateGame(game);

        try {
            Game savedGame = gameRepository.save(game);
            log.info("Juego '{}' guardado correctamente con ID: {}", game.getTitle(), savedGame.getId());
            return savedGame;
        } catch (DataAccessException e) {
            log.error("Error de base de datos al guardar juego: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el juego: problema de acceso a datos", e);
        } catch (Exception e) {
            log.error("Error inesperado al guardar juego: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el juego: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda un juego y devuelve su DTO
     * @param game Juego a guardar
     * @return DTO del juego guardado
     */
    @Transactional
    public GameResponseDTO saveAndGetDTO(Game game) {
        Game savedGame = save(game);
        return gameMapper.toResponseDTO(savedGame);
    }

    /**
     * Elimina un juego por su ID
     * @param id ID del juego a eliminar
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si el juego no existe
     */
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo");
        }

        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Juego no encontrado con ID: " + id);
        }
        
        try {
            gameRepository.deleteById(id);
            log.info("Juego con ID {} eliminado correctamente", id);
        } catch (DataAccessException e) {
            log.error("Error de base de datos al eliminar juego: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar el juego: problema de acceso a datos", e);
        }
    }

    /**
     * Actualiza un juego existente
     * @param game Nuevos datos del juego
     * @return Juego actualizado
     * @throws BadRequestException si el juego es inválido
     * @throws ResourceNotFoundException si el juego no existe
     */
    @Transactional
    public Game update(Game game) {
        validateGameForUpdate(game);

        try {
            Game updatedGame = gameRepository.save(game);
            log.info("Juego con ID {} actualizado correctamente", game.getId());
            return updatedGame;
        } catch (DataAccessException e) {
            log.error("Error de base de datos al actualizar juego: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar el juego: problema de acceso a datos", e);
        } catch (Exception e) {
            log.error("Error inesperado al actualizar juego: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar el juego: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un juego y devuelve su DTO
     * @param game Juego a actualizar
     * @return DTO del juego actualizado
     */
    @Transactional
    public GameResponseDTO updateAndGetDTO(Game game) {
        Game updatedGame = update(game);
        return gameMapper.toResponseDTO(updatedGame);
    }

    /**
     * Encuentra juegos de un usuario específico que tienen imágenes
     * @param userId ID del usuario
     * @return Lista de juegos con imágenes
     * @throws BadRequestException si el ID de usuario es nulo
     */
    @Transactional(readOnly = true)
    public List<Game> findByUserIdWithImages(Long userId) {
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        try {
            List<Game> games = gameRepository.findByUserIdAndImageIsNotNull(userId);
            log.info("Se encontraron {} juegos con imágenes para el usuario {}", games.size(), userId);
            return games;
        } catch (Exception e) {
            log.error("Error al buscar juegos con imágenes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al buscar juegos con imágenes: " + e.getMessage(), e);
        }
    }

    /**
     * Encuentra juegos de un usuario con imágenes y los convierte a DTOs
     * @param userId ID del usuario
     * @return Lista de DTOs de juegos con imágenes
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdWithImagesDTO(Long userId) {
        List<Game> games = findByUserIdWithImages(userId);
        return gameMapper.toResponseDTOList(games);
    }

    /**
     * Obtiene una lista de juegos por el ID del usuario
     * @param userId ID del usuario
     * @return Lista de juegos del usuario
     * @throws BadRequestException si el ID de usuario es nulo
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
     * Obtiene juegos por usuario y los convierte a DTOs
     * @param userId ID del usuario
     * @return Lista de DTOs de juegos
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdDTO(Long userId) {
        List<Game> games = findByUserId(userId);
        return gameMapper.toResponseDTOList(games);
    }

    /**
     * Valida un juego para creación
     * @param game Juego a validar
     * @throws BadRequestException si el juego es inválido
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
    }

    /**
     * Valida un juego para actualización
     * @param game Juego a validar
     * @throws BadRequestException si el juego es inválido
     * @throws ResourceNotFoundException si el juego no existe
     */
    private void validateGameForUpdate(Game game) {
        validateGame(game);
        
        if (game.getId() == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo para actualizarlo");
        }

        if (!gameRepository.existsById(game.getId())) {
            throw new ResourceNotFoundException("Juego no encontrado con ID: " + game.getId());
        }
    }
}
