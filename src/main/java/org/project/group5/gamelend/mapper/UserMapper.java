package org.project.group5.gamelend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.GameSummaryDTO;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.dto.UserSummaryDTO;
import org.project.group5.gamelend.entity.Game;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;

@Mapper(componentModel = "spring", uses = GameMapper.class)
public interface UserMapper {

    // Convierte User a UserResponseDTO
    @Mapping(target = "registrationDate", source = "registrationDate")
    @Mapping(target = "games", source = "games")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "gamesLent", source = "user", qualifiedByName = "getGamesLentQualified")
    UserResponseDTO toResponseDTO(User user);

    // Lista de User a lista de UserResponseDTO
    List<UserResponseDTO> toResponseDTOList(List<User> users);

    // Convierte User a UserSummaryDTO
    UserSummaryDTO toSummaryDTO(User user);

    // Lista de User a lista de UserSummaryDTO
    List<UserSummaryDTO> toSummaryDTOList(List<User> users);

    // Convierte UserDTO a User (para creación)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", source = "registrationDate")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User toEntity(UserDTO dto);

    // Actualiza un User con datos de UserDTO (no actualiza id ni registrationDate)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget User user);

    /**
     * Convierte una lista de entidades Role a una lista de nombres de rol (String).
     * Elimina el prefijo "ROLE_" de cada nombre.
     *
     * @param roles Lista de entidades Role
     * @return Lista de nombres de rol sin el prefijo "ROLE_"
     */
    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toCollection(java.util.LinkedList::new));
    }

    /**
     * Obtiene los juegos prestados activos de un usuario y los mapea a GameSummaryDTO.
     * Solo incluye préstamos que aún no han sido devueltos.
     *
     * @param user Usuario del que se obtienen los préstamos realizados
     * @return Lista de GameSummaryDTO de juegos prestados activos
     */
    @Named("getGamesLentQualified")
    default List<GameSummaryDTO> getGamesLentFromUser(User user) {
        if (user == null || user.getLoansMade() == null) return Collections.emptyList();
        return user.getLoansMade().stream()
                .filter(loan -> loan.getReturnDate() == null)
                .map(loan -> {
                    Game game = loan.getGame();
                    if (game == null) return null;
                    return new GameSummaryDTO(
                            game.getId(),
                            game.getTitle(),
                            game.getPlatform(),
                            game.getStatus()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.LinkedList::new));
    }
}
