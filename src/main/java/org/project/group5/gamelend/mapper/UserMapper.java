package org.project.group5.gamelend.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // ====== MÉTODOS DE ENTITY A DTO =======

    /**
     * Convierte una entidad User a UserResponseDTO
     */
    @Mapping(target = "registrationDate", source = "registrationDate", qualifiedByName = "userFormatDateTime")
    @Mapping(target = "games", source = "games") // MapStruct usará GameMapper para convertir List<Game> a List<GameResponseDTO>
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "gamesLent", source = "user", qualifiedByName = "getGamesLentQualified") // Usar source="user" para pasar el objeto completo
    UserResponseDTO toResponseDTO(User user);

    /**
     * Convierte una lista de User a lista de UserResponseDTO
     */
    List<UserResponseDTO> toResponseDTOList(List<User> users);

    /**
     * Convierte una entidad User a UserSummaryDTO
     * MapStruct mapeará id y publicName automáticamente.
     */
    UserSummaryDTO toSummaryDTO(User user);

    /**
     * Convierte una lista de User a lista de UserSummaryDTO
     */
    List<UserSummaryDTO> toSummaryDTOList(List<User> users);

    // ====== MÉTODOS DE DTO A ENTITY =======

    /**
     * Convierte UserDTO a entidad User (para creación)
     * Nota: UserDTO ahora es un record, se accede a los campos con dto.fieldName()
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", expression = "java(parseDateTime(dto.registrationDate()))") // Acceso a componente de record
    @Mapping(target = "roles", ignore = true) // Los roles se manejan en el servicio
    @Mapping(target = "games", ignore = true) // Los juegos se manejan en el servicio
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    // Otros campos como name, publicName, email, province, city, password se mapearán automáticamente
    // si los nombres coinciden entre UserDTO y User.
    User toEntity(UserDTO dto);

    /**
     * Actualiza una entidad User con datos del DTO
     * Nota: UserDTO ahora es un record, se accede a los campos con dto.fieldName()
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true) // Generalmente no se actualiza
    @Mapping(target = "password", ignore = true) // El password se actualiza por separado y con codificación
    @Mapping(target = "roles", ignore = true) // Los roles se manejan en el servicio
    @Mapping(target = "games", ignore = true) // Los juegos se manejan en el servicio
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget User user);

    // ====== MÉTODOS AUXILIARES =======

    /**
     * Formatea una fecha LocalDateTime a String
     */
    @Named("userFormatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Convierte una fecha String a LocalDateTime.
     * Si el string es nulo o vacío, o hay error de parseo, devuelve la fecha actual.
     * Considera si este es el comportamiento deseado o si debería lanzar una excepción/devolver null.
     */
    default LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            // Decide si devolver null, la fecha actual, o lanzar excepción
            return LocalDateTime.now(); // O return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            // Loggear el error e.printStackTrace(); o log.error("Error parsing date: {}", dateTimeString, e);
            return LocalDateTime.now(); // O return null; o lanzar una CustomParsingException
        }
    }

    /**
     * Convierte roles a lista de strings
     */
    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toCollection(java.util.LinkedList::new));
    }

    /**
     * Obtiene juegos prestados por el usuario (activos) y los mapea a GameSummaryDTO.
     * Renombrado para evitar conflicto de nombres si GameMapper también tuviera un método similar.
     */
    @Named("getGamesLentQualified")
    default List<GameSummaryDTO> getGamesLentFromUser(User user) { // Cambiado el nombre del método
        if (user == null || user.getLoansMade() == null) {
            return Collections.emptyList();
        }

        return user.getLoansMade().stream()
                .filter(loan -> loan.getReturnDate() == null) // Solo préstamos activos
                .map(loan -> {
                    Game game = loan.getGame();
                    if (game == null) return null; // Manejar caso de juego nulo en préstamo
                    // Asumiendo que GameSummaryDTO es un record:
                    // public record GameSummaryDTO(Long id, String title, String platform, Game.GameStatus status)
                    return new GameSummaryDTO(
                            game.getId(),
                            game.getTitle(),
                            game.getPlatform(),
                            game.getStatus()
                    );
                })
                .filter(java.util.Objects::nonNull) // Filtrar cualquier GameSummaryDTO nulo si el juego era nulo
                .collect(Collectors.toCollection(java.util.LinkedList::new));
    }
}
