package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.GameMapper;
import org.project.group5.gamelend.service.GameService;
import org.project.group5.gamelend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Controlador REST para la gestión de juegos
 */
@Slf4j
@RestController
@RequestMapping("api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final GameMapper gameMapper;

    /**
     * Obtiene todos los juegos registrados.
     * @return Lista de juegos.
     */
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        log.info("Solicitando lista de juegos");
        List<Game> games = gameService.findAll();
        
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(gameMapper.toResponseDTOList(games));
    }

    /**
     * Busca un juego por su título.
     * @param title Título del juego.
     * @return Juego encontrado.
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<GameResponseDTO> getGameByTitle(@PathVariable String title) {
        log.info("Buscando juego con título: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego es requerido");
        }
        
        Game game = gameService.findByTitle(title);
        if (game == null) {
            throw new ResourceNotFoundException("Juego no encontrado con título: " + title);
        }
        
        return ResponseEntity.ok(gameMapper.toResponseDTO(game));
    }

    /**
     * Obtiene juegos por usuario que tienen imagen.
     * @param userId ID del usuario.
     * @return Lista de juegos del usuario con imágenes.
     */
    @GetMapping("/user/{userId}/images")
    public ResponseEntity<List<GameResponseDTO>> getGamesByUserWithImages(@PathVariable Long userId) {
        log.info("Buscando juegos con imágenes para usuario con ID: {}", userId);
        
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }
        
        List<Game> games = gameService.findByUserIdWithImages(userId);
        
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }
        
        return ResponseEntity.ok(gameMapper.toResponseDTOList(games));
    }

    /**
     * Crea un nuevo juego a partir de un objeto GameDTO.
     * @param gameDTO Datos completos del juego.
     * @return Juego guardado.
     */
    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame(@Valid @RequestBody GameDTO gameDTO) {
        log.info("Guardando juego: {}", gameDTO.getTitle());
        
        // Validación de datos requeridos
        validateGameDTO(gameDTO);
        
        // Usar GameMapper para convertir DTO a entidad
        Game game = gameMapper.toEntity(gameDTO);
        game.setUser(userService.getCurrentUser());
        Game savedGame = gameService.save(game);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(gameMapper.toResponseDTO(savedGame));
    }

    /**
     * Actualiza un juego existente.
     * @param id ID del juego a actualizar.
     * @param gameDTO Datos actualizados del juego.
     * @return Juego actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDTO> updateGame(
            @PathVariable Long id, 
            @Valid @RequestBody GameDTO gameDTO) {
        log.info("Actualizando juego con ID: {}", id);
        
        Game game = gameService.findById(id);
        
        // Usar GameMapper para actualizar la entidad con los datos del DTO
        gameMapper.updateGameFromDto(gameDTO, game);
        
        Game updatedGame = gameService.update(game);
        
        return ResponseEntity.ok(gameMapper.toResponseDTO(updatedGame));
    }

    /**
     * Elimina un juego por su ID.
     * @param id ID del juego a eliminar.
     * @return Mensaje de confirmación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable Long id) {
        log.info("Eliminando juego con ID: {}", id);
        
        gameService.deleteById(id);
        return ResponseEntity.ok("Juego eliminado correctamente");
    }
    
    /**
     * Valida que un DTO de juego tenga los campos obligatorios
     * @param gameDTO DTO a validar
     * @throws BadRequestException si faltan campos obligatorios
     */
    private void validateGameDTO(GameDTO gameDTO) {
        if (gameDTO == null) {
            throw new BadRequestException("Los datos del juego son requeridos");
        }
        if (gameDTO.getTitle() == null || gameDTO.getTitle().trim().isEmpty()) {
            throw new BadRequestException("El título del juego es requerido");
        }
        if (gameDTO.getPlatform() == null || gameDTO.getPlatform().trim().isEmpty()) {
            throw new BadRequestException("La plataforma del juego es requerida");
        }
    }
}