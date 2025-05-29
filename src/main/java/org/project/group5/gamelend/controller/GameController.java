package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.mapper.GameMapper;
import org.project.group5.gamelend.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de juegos.
 * Maneja operaciones CRUD y búsquedas.
 */
@Slf4j
@RestController
@RequestMapping("api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    /**
     * Lista todos los juegos disponibles
     */
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        log.info("Solicitando lista de todos los juegos");
        List<Game> gamesEntities = gameService.findAll();
        List<GameResponseDTO> gameDTOs = gameMapper.toResponseDTOList(gamesEntities);
        return ResponseEntity.ok(gameDTOs);
    }

    /**
     * Obtiene un juego por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable Long id) {
        log.info("Solicitando detalles para el juego ID: {}", id);
        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo");
        }
        GameResponseDTO gameResponseDTO = gameService.findByIdDTO(id);
        return ResponseEntity.ok(gameResponseDTO);
    }

    /**
     * Busca un juego por título
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<GameResponseDTO> getGameByTitle(@PathVariable String title) {
        log.info("Buscando juego con título: {}", title);
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título es requerido");
        }
        GameResponseDTO gameResponseDTO = gameService.findByTitleDTO(title);
        return ResponseEntity.ok(gameResponseDTO);
    }

    /**
     * Lista los juegos de un usuario específico
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GameResponseDTO>> getGamesByUserId(@PathVariable Long userId) {
        log.info("Solicitando juegos del usuario ID: {}", userId);
        if (userId == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo");
        }
        List<GameResponseDTO> userGames = gameService.findByUserIdDTO(userId);
        return ResponseEntity.ok(userGames);
    }

    /**
     * Lista los juegos con imágenes de un usuario
     */
    @GetMapping("/user/{userId}/images")
    public ResponseEntity<List<GameResponseDTO>> getGamesByUserWithImages(@PathVariable Long userId) {
        log.info("Buscando juegos con imágenes del usuario ID: {}", userId);
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }
        List<GameResponseDTO> gameDTOs = gameService.findByUserIdWithImagesDTO(userId);
        return gameDTOs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(gameDTOs);
    }

    /**
     * Crea un nuevo juego
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GameResponseDTO> createGame(@Valid @RequestBody GameDTO gameDTO) {
        log.info("Creando juego: {}", gameDTO.title());
        GameResponseDTO savedGame = gameService.createGameFromDTO(gameDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    /**
     * Actualiza un juego existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GameResponseDTO> updateGame(
            @PathVariable Long id,
            @Valid @RequestBody GameDTO gameDTO) {
        log.info("Actualizando juego ID: {} con datos: {}", id, gameDTO);
        GameResponseDTO updatedGame = gameService.updateGameFromDTO(id, gameDTO);
        return ResponseEntity.ok(updatedGame);
    }

    /**
     * Elimina un juego
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        log.info("Eliminando juego ID: {}", id);
        if (id == null) {
            throw new BadRequestException("ID del juego no puede ser nulo");
        }
        gameService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
