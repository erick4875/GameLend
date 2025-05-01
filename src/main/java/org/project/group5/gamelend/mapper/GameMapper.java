package org.project.group5.gamelend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.User;

@Mapper(componentModel = "spring")
public interface GameMapper {

    // MÉTODOS DE ENTITY A DTO

    /**
     * Convierte una entidad Game a GameResponseDTO (versión completa, respuesta)
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.publicName")
    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "imagePath", source = "imagePath")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "isCatalog", expression = "java(game.isCatalog())")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "genre", source = "genre")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    GameResponseDTO toResponseDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameResponseDTO (versión completa, respuesta)
     */
    List<GameResponseDTO> toResponseDTOList(List<Game> games);

    /**
     * Convierte una entidad Game a GameSummaryDTO (resumen, respuesta)
     */
    @Mapping(target = "status", expression = "java(game.getStatus() != null ? game.getStatus().toString() : null)")
    GameSummaryDTO toSummaryDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameSummaryDTO
     */
    List<GameSummaryDTO> toSummaryDTOList(List<Game> games);

    // MÉTODOS DE DTO A ENTITY

    /**
     * Convierte GameDTO a entidad Game
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    @Mapping(target = "isCatalog", constant = "false")
    Game toEntity(GameDTO dto);

    /**
     * Actualiza una entidad Game existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    void updateGameFromDto(GameDTO dto, @MappingTarget Game game);

    /**
     * Crea un juego de usuario a partir de un juego de catálogo
     */
    @Named("createUserGameFromCatalog")
    default Game createUserGameFromCatalog(Game catalogGame, User user) {
        if (catalogGame == null) {
            return null;
        }

        return Game.builder()
                .title(catalogGame.getTitle())
                .platform(catalogGame.getPlatform())
                .genre(catalogGame.getGenre())
                .description(catalogGame.getDescription())
                .image(catalogGame.getImage())
                .catalogGame(catalogGame)
                .user(user)
                .status(Game.GameStatus.AVAILABLE)
                .build();
    }

    // Métodos auxiliares

    /**
     * Método auxiliar para convertir un Document a su ID
     */
    default Long documentToId(Document document) {
        return document != null ? document.getId() : null;
    }

    /**
     * Método auxiliar para convertir un User a su ID
     */
    default Long userToId(User user) {
        return user != null ? user.getId() : null;
    }

    /**
     * Método auxiliar para convertir un User a su nombre público
     */
    default String userToName(User user) {
        return user != null ? user.getPublicName() : null;
    }

    /**
     * Método auxiliar para formatear URL de imagen
     */
    @Named("formatImageUrl")
    default String formatImageUrl(String path) {
        if (path == null || path.isEmpty()) {
            return "/images/default-game.png";
        }

        if (path.startsWith("http")) {
            return path;
        }

        return "/api/images/" + path;
    }
}
