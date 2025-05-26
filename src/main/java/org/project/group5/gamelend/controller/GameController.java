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

@Slf4j
@RestController
@RequestMapping("api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        log.info("Solicitando lista de todos los juegos");
        List<Game> gamesEntities = gameService.findAll(); // Asume que GameService tiene findAll()
        List<GameResponseDTO> gameDTOs = gameMapper.toResponseDTOList(gamesEntities);
        return ResponseEntity.ok(gameDTOs);
    }

    /**
     * Obtiene los detalles de un juego específico por su ID.
     * 
     * @param id El ID del juego a obtener.
     * @return ResponseEntity con GameResponseDTO si se encuentra, o 404 si no.
     */
    @GetMapping("/{id}")
    // @PreAuthorize("isAuthenticated()") // Opcional: Si ver detalles requiere
    // autenticación
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable Long id) {
        log.info("Solicitando detalles para el juego con ID: {}", id);
        if (id == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo.");
        }
        // Asumimos que GameService tiene un método findByIdDTO o similar
        GameResponseDTO gameResponseDTO = gameService.findByIdDTO(id);
        return ResponseEntity.ok(gameResponseDTO);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<GameResponseDTO> getGameByTitle(@PathVariable String title) {
        log.info("Buscando juego con título: {}", title);
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego es requerido para la búsqueda");
        }
        // Asumimos que GameService tiene findByTitleDTO o similar
        GameResponseDTO gameResponseDTO = gameService.findByTitleDTO(title);
        return ResponseEntity.ok(gameResponseDTO);
    }

    /**
     * Obtiene todos los juegos registrados para un usuario específico.
     * 
     * @param userId El ID del usuario.
     * @return Lista de GameResponseDTO para ese usuario.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden ver listas de juegos de otros
    public ResponseEntity<List<GameResponseDTO>> getGamesByUserId(@PathVariable Long userId) {
        log.info("Solicitando lista de juegos para el usuario ID: {}", userId);
        if (userId == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        List<GameResponseDTO> userGames = gameService.findByUserIdDTO(userId); // Este método ya lo tienes en
                                                                               // GameService
        // Devolver 200 OK con lista vacía [] si el usuario no tiene juegos.
        return ResponseEntity.ok(userGames);
    }

    @GetMapping("/user/{userId}/images")
    public ResponseEntity<List<GameResponseDTO>> getGamesByUserWithImages(@PathVariable Long userId) {
        log.info("Buscando juegos con imágenes para usuario con ID: {}", userId);
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }
        // Asumimos que GameService tiene findByUserIdWithImagesDTO o similar
        List<GameResponseDTO> gameDTOs = gameService.findByUserIdWithImagesDTO(userId);
        if (gameDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gameDTOs);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden eliminar juegos
    public ResponseEntity<GameResponseDTO> createGame(@Valid @RequestBody GameDTO gameDTO) {
        log.info("Creando nuevo juego con título: {}", gameDTO.title());
        // La lógica de asociar el usuario y guardar ya está en GameService.create o
        // similar
        GameResponseDTO savedGameResponseDTO = gameService.createGameFromDTO(gameDTO); // Asume que existe este método
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGameResponseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden eliminar juegos
    public ResponseEntity<GameResponseDTO> updateGame(
            @PathVariable Long id,
            @Valid @RequestBody GameDTO gameDTO) {
        log.info("Actualizando juego con ID: {} con datos: {}", id, gameDTO);
        GameResponseDTO updatedGameResponseDTO = gameService.updateGameFromDTO(id, gameDTO);
        return ResponseEntity.ok(updatedGameResponseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden eliminar juegos
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        log.info("Eliminando juego con ID: {}", id);
        if (id == null) {
            throw new BadRequestException("ID del juego no puede ser nulo para eliminar.");
        }
        gameService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
