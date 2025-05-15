package org.project.group5.gamelend.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.project.group5.gamelend.dto.GameDTO;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.User;

/**
 * Mapper para convertir entre entidades Game y sus DTOs usando MapStruct.
 */
@Mapper(componentModel = "spring")
public interface GameMapper {

    /**
     * Convierte Game a GameResponseDTO.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.publicName")
    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "imagePath", source = "image.completeFileName", qualifiedByName = "formatImageUrl")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "catalog", source = "catalog")
    GameResponseDTO toResponseDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameResponseDTO.
     */
    List<GameResponseDTO> toResponseDTOList(List<Game> games);

    /**
     * Convierte Game a GameSummaryDTO.
     */
    GameSummaryDTO toSummaryDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameSummaryDTO.
     */
    List<GameSummaryDTO> toSummaryDTOList(List<Game> games);

    /**
     * Convierte Game a GameDTO.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "imagePath", source = "image.completeFileName", qualifiedByName = "formatImageUrl")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "catalog", source = "catalog")
    GameDTO toDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameDTO.
     */
    List<GameDTO> toDTOList(List<Game> games);

    /**
     * Convierte GameDTO a Game (para creación).
     * Relaciones como user, image y catalogGame se deben establecer en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    Game toEntity(GameDTO dto);

    /**
     * Actualiza una entidad Game existente con datos de GameDTO.
     * Ignora nulos para no sobrescribir campos existentes.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "catalog", source = "catalog")
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateGameFromDto(GameDTO dto, @MappingTarget Game game);

    /**
     * Crea un juego de usuario a partir de un juego de catálogo.
     */
    @Named("createUserGameFromCatalog")
    default Game createUserGameFromCatalog(Game catalogGame, User user) {
        if (catalogGame == null || !catalogGame.isCatalog()) {
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
                .catalog(false)
                .status(Game.GameStatus.AVAILABLE)
                .build();
    }

    /**
     * Devuelve la URL de la imagen o una por defecto si no hay imagen.
     */
    @Named("formatImageUrl")
    default String formatImageUrl(String completeFileName) {
        if (completeFileName == null || completeFileName.isEmpty()) {
            return "/api/documents/download/default-game-image.png";
        }
        if (completeFileName.startsWith("http://") || completeFileName.startsWith("https://")) {
            return completeFileName;
        }
        return "/api/documents/download/" + completeFileName;
    }
}
