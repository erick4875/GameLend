package org.project.group5.gamelend.service;

import java.util.List;

import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.GameMapper;
import org.project.group5.gamelend.repository.GameRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service; // Para ResponseStatusException
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Para ResponseStatusException

/**
 * Servicio para la gestión de juegos en la aplicación.
 */
@Service
@Transactional // Aplicar transaccionalidad a nivel de clase por defecto
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final UserService userService; // Para obtener la entidad User
    private final DocumentService documentService; // Para obtener la entidad Document si se maneja imageId

    /**
     * Obtiene todos los juegos registrados.
     * @return Lista de entidades Game.
     */
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        List<Game> games = gameRepository.findAll();
        if (games.isEmpty()) {
            log.info("No se encontraron juegos en la base de datos.");
        } else {
            log.debug("Se encontraron {} juegos.", games.size());
        }
        return games;
    }

    /**
     * Encuentra todos los juegos y los convierte a una lista de GameResponseDTO.
     * @return Lista de GameResponseDTO.
     */
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findAllDTO() {
        List<Game> games = findAll(); // Reutiliza el método findAll()
        return gameMapper.toResponseDTOList(games);
    }

    /**
     * Busca un juego por su título.
     * @param title Título del juego a buscar.
     * @return Entidad Game encontrada.
     */
    @Transactional(readOnly = true)
    public Game findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("El título del juego no puede ser nulo o vacío.");
        }
        return gameRepository.findByTitleIgnoreCase(title)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con título: " + title));
    }

    /**
     * Busca un juego por su ID y lo convierte a GameResponseDTO.
     * @param id ID del juego.
     * @return GameResponseDTO del juego.
     */
    @Transactional(readOnly = true)
    public GameResponseDTO findByIdDTO(Long id) {
        Game game = findById(id); // Reutiliza el método findById(Long id)
        return gameMapper.toResponseDTO(game);
    }

    /**
     * Busca un juego por su título y lo convierte a GameResponseDTO.
     * @param title Título del juego.
     * @return GameResponseDTO del juego.
     */
    @Transactional(readOnly = true)
    public GameResponseDTO findByTitleDTO(String title) {
        Game game = findByTitle(title);
        return gameMapper.toResponseDTO(game);
    }

    /**
     * Busca una entidad Game por su ID.
     * @param id ID del juego.
     * @return Entidad Game.
     */
    @Transactional(readOnly = true)
    public Game findById(Long id) {
        if (id == null) {
            log.warn("Intento de buscar juego con ID nulo.");
            throw new BadRequestException("El ID del juego no puede ser nulo.");
        }
        return gameRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Juego no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Juego no encontrado con ID: " + id);
                });
    }

    /**
     * Valida los datos básicos de un juego antes de guardarlo o actualizarlo.
     * @param game Entidad Game a validar.
     */
    private void validateGame(Game game) {
        if (game == null) {
            throw new BadRequestException("El juego no puede ser nulo.");
        }
        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            throw new BadRequestException("El título del juego es obligatorio.");
        }
        if (game.getPlatform() == null || game.getPlatform().trim().isEmpty()) {
            throw new BadRequestException("La plataforma del juego es obligatoria.");
        }
        // Añadir más validaciones si es necesario (ej. género, estado, etc.)
    }

    /**
     * Valida un juego para una operación de actualización.
     * @param game Entidad Game a validar.
     */
    private void validateGameForUpdate(Game game) {
        validateGame(game); // Reutiliza las validaciones básicas
        if (game.getId() == null) {
            throw new BadRequestException("El ID del juego no puede ser nulo para actualizarlo.");
        }
        if (!gameRepository.existsById(game.getId())) {
            throw new ResourceNotFoundException("Juego no encontrado con ID: " + game.getId() + " para actualizar.");
        }
    }
    
    /**
     * Guarda (crea o actualiza) una entidad Game en la base de datos.
     * Este método es más genérico y espera que la entidad Game ya esté preparada.
     * @param game Entidad Game a guardar.
     * @return La entidad Game guardada.
     */
    @Transactional
    public Game save(Game game) { // Este método es llamado por createGameFromDTO y updateGameFromDTO
        // Las validaciones específicas de creación (ej. unicidad de título si aplica) o
        // actualización (ej. si el juego existe) se manejan en los métodos llamadores.
        // validateGame(game); // Validaciones básicas de campos obligatorios
        try {
            Game savedGame = gameRepository.save(game);
            log.info("Juego '{}' guardado/actualizado con ID: {}", savedGame.getTitle(), savedGame.getId());
            return savedGame;
        } catch (DataAccessException e) {
            log.error("Error de BD al guardar/actualizar juego '{}': {}", game.getTitle(), e.getMessage(), e);
            // Aquí podrías querer manejar DataIntegrityViolationException específicamente si tienes constraints únicas
            throw new RuntimeException("Error de base de datos al guardar/actualizar el juego.", e);
        } catch (Exception e) {
            log.error("Error inesperado al guardar/actualizar juego '{}': {}", game.getTitle(), e.getMessage(), e);
            throw new RuntimeException("Error inesperado al guardar/actualizar el juego.", e);
        }
    }

    /**
     * Crea un nuevo juego a partir de un GameDTO y devuelve GameResponseDTO.
     * @param gameDTO Datos del juego a crear.
     * @return GameResponseDTO del juego guardado.
     */
    @Transactional
    public GameResponseDTO createGameFromDTO(GameDTO gameDTO) {
        log.info("Servicio: Creando nuevo juego con título: {}", gameDTO.title());

        if (gameDTO.userId() == null) {
            throw new BadRequestException("El ID del usuario es requerido para crear un juego.");
        }

        Game gameToCreate = gameMapper.toEntity(gameDTO); // Mapea los campos básicos del DTO
        
        User owner = userService.getUserById(gameDTO.userId()); // Obtiene la entidad User
        gameToCreate.setUser(owner); // Asigna el propietario

        // Manejar la imagen si se proporciona un imageId
        if (gameDTO.imageId() != null) {
            Document imageDocument = documentService.find(gameDTO.imageId()); // Asume que DocumentService.find() existe
            gameToCreate.setImage(imageDocument);
        }
        // Manejar catalogGame si se proporciona catalogGameId
        if (gameDTO.catalogGameId() != null) {
            Game catalogGameRef = findById(gameDTO.catalogGameId()); // Reutiliza findById
            gameToCreate.setCatalogGame(catalogGameRef);
        }
        // El campo 'catalog' (boolean) ya debería haber sido mapeado por gameMapper.toEntity(gameDTO) si existe en GameDTO

        validateGame(gameToCreate); // Valida la entidad Game antes de guardarla

        Game savedGame = gameRepository.save(gameToCreate); // Guarda la entidad Game preparada
        return gameMapper.toResponseDTO(savedGame); // Mapea la entidad guardada a DTO de respuesta
    }

    /**
     * Actualiza un juego existente con datos de un GameDTO y devuelve GameResponseDTO.
     * @param id ID del juego a actualizar.
     * @param gameDTO DTO con los nuevos datos del juego.
     * @return GameResponseDTO del juego actualizado.
     */
    @Transactional
    public GameResponseDTO updateGameFromDTO(Long id, GameDTO gameDTO) {
        log.info("Servicio: Actualizando juego con ID: {} con datos: {}", id, gameDTO);

        Game gameToUpdate = findById(id); // Carga la entidad existente, lanza excepción si no se encuentra

        // gameMapper.updateGameFromDto actualiza los campos de gameToUpdate con los de gameDTO
        // (ignora nulos en el DTO si así está configurado el mapper)
        gameMapper.updateGameFromDto(gameDTO, gameToUpdate);

        // Re-asignar relaciones si es necesario y si el DTO las trae para modificar
        // Propietario (User): Generalmente no se cambia el propietario de un juego existente de esta forma.
        // Si el gameDTO.userId() es solo para referencia o validación, no para cambiar el dueño.
        if (gameDTO.userId() != null) {
            if (gameToUpdate.getUser() == null || !gameToUpdate.getUser().getId().equals(gameDTO.userId())) {
                // Esta lógica implica que se puede cambiar el dueño del juego.
                // Considera las implicaciones y si esto es un caso de uso válido.
                // Si solo se actualizan los datos del juego y no su propietario, esta parte no sería necesaria
                // o necesitaría validaciones adicionales (ej. solo un admin puede cambiar el propietario).
                log.warn("Actualizando propietario para juego ID {} a usuario ID {}. Verificar si es el comportamiento deseado.", id, gameDTO.userId());
                User newOwner = userService.getUserById(gameDTO.userId());
                gameToUpdate.setUser(newOwner);
            }
        }

        // Juego de Catálogo Base
        if (gameDTO.catalogGameId() != null) {
            Game catalogGameRef = findById(gameDTO.catalogGameId());
            gameToUpdate.setCatalogGame(catalogGameRef);
        } else {
            gameToUpdate.setCatalogGame(null); // Permitir desvincular de un juego de catálogo
        }

        // Imagen
        if (gameDTO.imageId() != null) {
            Document imageDocument = documentService.find(gameDTO.imageId());
            gameToUpdate.setImage(imageDocument);
        } else if (gameDTO.imageUrl() == null || gameDTO.imageUrl().isBlank()) {
            // Si no se envía imageId y no se envía imageUrl (o está vacío),
            // se podría interpretar como eliminar la imagen actual.
            gameToUpdate.setImage(null);
        }
        // La lógica para manejar imageUrl (si es una nueva URL a procesar) sería más compleja.

        // Campo 'catalog' (boolean)
        if (gameDTO.catalog() != null) { // El mapper ya debería haberlo manejado si está en GameDTO
            gameToUpdate.setCatalog(gameDTO.catalog());
        }
        
        validateGameForUpdate(gameToUpdate); // Valida la entidad Game antes de guardarla

        Game updatedGame = gameRepository.save(gameToUpdate); // Guarda la entidad Game actualizada
        return gameMapper.toResponseDTO(updatedGame); // Mapea a DTO de respuesta
    }


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

    // Métodos para búsquedas específicas que devuelven DTOs
    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdWithImagesDTO(Long userId) {
        List<Game> games = findByUserIdWithImages(userId);
        return gameMapper.toResponseDTOList(games);
    }

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

    @Transactional(readOnly = true)
    public List<GameResponseDTO> findByUserIdDTO(Long userId) {
        List<Game> games = findByUserId(userId);
        return gameMapper.toResponseDTOList(games);
    }
    
    @Transactional(readOnly = true)
    public List<Game> findByUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }
        List<Game> games = gameRepository.findByUserId(userId);
        log.info("Se encontraron {} juegos para el usuario {}", games.size(), userId);
        return games;
    }
}