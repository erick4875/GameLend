package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.exception.BadRequestException;
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
     * @return Lista de GameResponseDTO.
     */
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        log.info("Solicitando lista de todos los juegos");
        List<Game> games = gameService.findAll();
        
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gameMapper.toResponseDTOList(games));
    }

    /**
     * Busca un juego por su título.
     * @param title Título del juego.
     * @return GameResponseDTO del juego encontrado.
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<GameResponseDTO> getGameByTitle(@PathVariable String title) {
        log.info("Buscando juego con título: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego es requerido para la búsqueda");
        }
        
        Game game = gameService.findByTitle(title);
        return ResponseEntity.ok(gameMapper.toResponseDTO(game));
    }

    /**
     * Obtiene juegos por usuario que tienen imagen.
     * @param userId ID del usuario.
     * @return Lista de GameResponseDTO de los juegos del usuario con imágenes.
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
     * Las validaciones del DTO se realizan mediante @Valid.
     * @param gameDTO Datos del juego a crear.
     * @return GameResponseDTO del juego guardado.
     */
    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame(@Valid @RequestBody GameDTO gameDTO) {
        log.info("Creando nuevo juego con título: {}", gameDTO.title());
        
        Game gameToCreate = gameMapper.toEntity(gameDTO);

        if (gameDTO.userId() == null) {
            throw new BadRequestException("El ID del usuario es requerido para crear un juego.");
        }
        gameToCreate.setUser(userService.findById(gameDTO.userId()));

        Game savedGame = gameService.save(gameToCreate);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(gameMapper.toResponseDTO(savedGame));
    }

    /**
     * Actualiza un juego existente.
     * Las validaciones del DTO se realizan mediante @Valid.
     * @param id ID del juego a actualizar.
     * @param gameDTO Datos actualizados del juego.
     * @return GameResponseDTO del juego actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDTO> updateGame(
            @PathVariable Long id, 
            @Valid @RequestBody GameDTO gameDTO) {
        log.info("Actualizando juego con ID: {} con datos: {}", id, gameDTO);

        Game updatedGame = gameService.update(id, gameDTO);
        
        return ResponseEntity.ok(gameMapper.toResponseDTO(updatedGame));
    }

    /**
     * Elimina un juego por su ID.
     * @param id ID del juego a eliminar.
     * @return Mensaje de confirmación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        log.info("Eliminando juego con ID: {}", id);
        if (id == null) {
            throw new BadRequestException("ID del juego no puede ser nulo para eliminar.");
        }
        gameService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}