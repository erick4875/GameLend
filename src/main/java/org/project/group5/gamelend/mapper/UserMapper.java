package org.project.group5.gamelend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.GameResponseDTO;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Loan;
import org.project.group5.gamelend.entity.User;

/**
 * Mapper para convertir entre User y sus DTOs
 */
@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {

    /**
     * Convierte User a UserResponseDTO
     */
    @Mapping(target = "registrationDate", source = "registrationDate")
    @Mapping(target = "games", source = "games", qualifiedByName = "mapGamesToResponseDTOs")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStrings")
    @Mapping(target = "gamesLent", source = "loansMade", qualifiedByName = "mapLoansToGamesLent")
    UserResponseDTO toResponseDTO(User user);

    /**
     * Lista de User a lista de UserResponseDTO
     */
    List<UserResponseDTO> toResponseDTOList(List<User> users);

    /**
     * Método para convertir User a UserSummaryDTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "publicName", source = "publicName")
    UserSummaryDTO toUserSummaryDTO(User user);

    /**
     * Lista de User a lista de UserSummaryDTO
     */
    List<UserSummaryDTO> toSummaryDTOList(List<User> users);

    /**
     * Convierte UserDTO a User (para creación)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "publicName", source = "publicName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "registrationDate", source = "registrationDate")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User toEntity(UserDTO dto);

    /**
     * Actualiza un User con datos de UserDTO (no actualiza id ni registrationDate)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "publicName", source = "publicName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget User user);

    /**
     * Convierte préstamos a juegos prestados (DTOs resumidos)
     */
    @Named("mapLoansToGamesLent")
    default List<GameSummaryDTO> mapLoansToGamesLent(List<Loan> loansMade) {
        if (loansMade == null) {
            return Collections.emptyList();
        }
        return loansMade.stream()
                .filter(loan -> loan != null && loan.getReturnDate() == null && loan.getGame() != null)
                .map(loan -> {
                    Game game = loan.getGame();
                    return new GameSummaryDTO(
                            game.getId(),
                            game.getTitle(),
                            game.getPlatform(),
                            game.getStatus());
                })
                .collect(Collectors.toList());
    }

    /**
     * Mapeo individual de Game a GameResponseDTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.publicName")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "imageId", expression = "java(game.getImage() != null ? game.getImage().getId() : null)")
    @Mapping(target = "imageUrl", expression = "java(buildDocumentUrl(game.getImage()))")
    GameResponseDTO gameToGameResponseDTO(Game game);

    /**
     * Convierte lista de juegos a lista de DTOs de respuesta de juegos
     */
    @Named("mapGamesToResponseDTOs")
    default List<GameResponseDTO> mapGamesToResponseDTOs(List<Game> games) {
        if (games == null) {
            return Collections.emptyList();
        }

        return games.stream()
                .filter(game -> game != null)
                .map(this::gameToGameResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método para mapear roles a strings
     */
    @Named("mapRolesToStrings")
    default List<String> mapRolesToStrings(List<?> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        
        return roles.stream()
            .filter(role -> role != null)
            .map(role -> {
                // Obtener el nombre del rol usando reflection o cast según la estructura
                try {
                    return role.getClass().getMethod("getName").invoke(role).toString();
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    return "ROLE_USER"; // Valor por defecto en caso de error
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Construye la URL completa para un documento
     */
    @Named("buildDocumentUrl")
    default String buildDocumentUrl(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        return "/api/documents/" + document.getId();
    }
}