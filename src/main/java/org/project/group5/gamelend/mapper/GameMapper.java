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

@Mapper(componentModel = "spring")
public interface GameMapper {

    // MÉTODOS DE ENTITY A DTO (Records)

    /**
     * Convierte una entidad Game a GameResponseDTO (record).
     * MapStruct llamará al constructor canónico del record.
     * Los campos con el mismo nombre y tipo se mapean automáticamente.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.publicName")
    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "imagePath", source = "image.completeFileName", qualifiedByName = "formatImageUrl")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "catalog", source = "catalog")
    GameResponseDTO toResponseDTO(Game game);

    List<GameResponseDTO> toResponseDTOList(List<Game> games);

    /**
     * Convierte una entidad Game a GameSummaryDTO (record).
     * El campo 'status' (GameStatus) se mapeará directamente.
     * 'id', 'title', 'platform' se mapean automáticamente.
     */
    GameSummaryDTO toSummaryDTO(Game game);

    List<GameSummaryDTO> toSummaryDTOList(List<Game> games);

    // MÉTODOS DE ENTITY A DTO (GameDTO)

    /**
     * Convierte una entidad Game a GameDTO.
     * Ajusta los mappings según la definición de tu GameDTO.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "imagePath", source = "image.completeFileName", qualifiedByName = "formatImageUrl")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "catalog", source = "catalog")
    GameDTO toDTO(Game game);

    List<GameDTO> toDTOList(List<Game> games);

    // MÉTODOS DE DTO (Record) A ENTITY

    /**
     * Convierte GameDTO (record) a entidad Game para creación.
     * Las relaciones (user, image, catalogGame) y el id se ignoran aquí
     * y deben establecerse en el servicio.
     * El campo 'status' (GameStatus) se mapeará directamente desde dto.status().
     * El campo 'isCatalog' (Boolean) del DTO se mapeará al campo 'isCatalog' (boolean o Boolean) de la entidad.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    Game toEntity(GameDTO dto);

    /**
     * Actualiza una entidad Game existente con datos del GameDTO (record).
     * Las relaciones (user, image, catalogGame) y el id se ignoran aquí
     * y deben establecerse en el servicio después de llamar a este método.
     * Se usa BeanMapping para ignorar nulos del DTO y no sobrescribir campos existentes en la entidad.
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

    // Métodos auxiliares

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
