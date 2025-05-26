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
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.User;
import org.springframework.beans.factory.annotation.Value;

/**
 * Mapper para convertir entre entidades Game y sus DTOs usando MapStruct.
 */
@Mapper(componentModel = "spring")
public abstract class GameMapper {

    @Value("${app.image-base-url:http://localhost:8081}")
    protected String imageBaseUrl;

    /**
     * Convierte Game a GameResponseDTO.
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.publicName", target = "userName")
    @Mapping(source = "catalogGame.id", target = "catalogGameId")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image", target = "imageUrl", qualifiedByName = "buildDocumentUrl")
    public abstract GameResponseDTO toResponseDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameResponseDTO.
     */
    public abstract List<GameResponseDTO> toResponseDTOList(List<Game> games);

    /**
     * Convierte Game a GameSummaryDTO.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "status", source = "status")
    public abstract GameSummaryDTO toGameSummaryDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameSummaryDTO.
     */
    public abstract List<GameSummaryDTO> toSummaryDTOList(List<Game> games);

    /**
     * Convierte Game a GameDTO.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image", target = "imageUrl", qualifiedByName = "buildDocumentUrl")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "catalog", source = "catalog")
    public abstract GameDTO toDTO(Game game);

    /**
     * Convierte una lista de Game a una lista de GameDTO.
     */
    public abstract List<GameDTO> toDTOList(List<Game> games);

    /**
     * Convierte GameDTO a Game (para creación).
     * Relaciones como user, image y catalogGame se deben establecer en el servicio.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "catalogGame", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    public abstract Game toEntity(GameDTO dto);

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
    public abstract void updateGameFromDto(GameDTO dto, @MappingTarget Game game);

    /**
     * Crea un juego de usuario a partir de un juego de catálogo.
     */
    @Named("createUserGameFromCatalog")
    public Game createUserGameFromCatalog(Game catalogGame, User user) {
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
     * Construye la URL completa para un documento.
     */
    @Named("buildDocumentUrl")
    public String buildDocumentUrl(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        return imageBaseUrl + "/api/documents/" + document.getId();
    }

}
