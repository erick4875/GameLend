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
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
/**
 * Mapper para convertir entre User y sus DTOs
 */
// Si GameMapper tiene el @Named("buildDocumentUrl"), necesitas añadirlo a uses:
// Ejemplo: @Mapper(componentModel = "spring", uses = { RoleMapper.class, GameMapper.class })
// O, si mueves buildDocumentUrl a este UserMapper (como está ahora), no necesitas GameMapper en uses para ESO.
@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {


    /**
     * Convierte User a UserResponseDTO
     */
    @Mapping(target = "registrationDate", source = "registrationDate") // Asegúrate que el formato sea compatible o usa un formateador
    @Mapping(target = "games", source = "games", qualifiedByName = "mapGamesToResponseDTOs")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "gamesLent", source = "loansMade", qualifiedByName = "mapLoansToGamesLent")
    @Mapping(target = "profileImageUrl", source = "profileImage", qualifiedByName = "buildDocumentUrlFromUserEntity")
    UserResponseDTO toResponseDTO(User user);

    List<UserResponseDTO> toResponseDTOList(List<User> users);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "publicName", source = "publicName")
    // Si UserSummaryDTO también necesita profileImageUrl:
    // @Mapping(target = "profileImageUrl", source = "profileImage", qualifiedByName = "buildDocumentUrlFromUserEntity")
    UserSummaryDTO toUserSummaryDTO(User user);

    List<UserSummaryDTO> toSummaryDTOList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "publicName", source = "publicName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password") // El servicio lo encriptará
    @Mapping(target = "province", source = "province")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "roles", ignore = true) 
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "password", ignore = true)    // La contraseña se actualiza por separado o no aquí
    @Mapping(target = "email", ignore = true)       // El email generalmente no se actualiza desde este flujo
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "profileImage", ignore = true) // La imagen de perfil se actualiza por separado
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget User user);

    @Named("mapLoansToGamesLent")
    default List<GameSummaryDTO> mapLoansToGamesLent(List<Loan> loansMade) {
        if (loansMade == null) {
            return Collections.emptyList();
        }
        return loansMade.stream()
                .filter(loan -> loan != null && loan.getReturnDate() == null && loan.getGame() != null)
                .map(loan -> {
                    Game game = loan.getGame();
                    // Asumimos que GameSummaryDTO tiene este constructor o campos
                    return new GameSummaryDTO(
                            game.getId(),
                            game.getTitle(),
                            game.getPlatform(),
                            game.getStatus());
                })
                .collect(Collectors.toList());
    }

    // Este método gameToGameResponseDTO podría estar en un GameMapper e importarse con 'uses'
    // Si lo dejas aquí, asegúrate de que las dependencias (como buildDocumentUrl) sean accesibles.
    // He renombrado tu buildDocumentUrl para evitar conflicto con el de GameMapper si lo tuvieras.
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.publicName")
    @Mapping(target = "catalogGameId", source = "catalogGame.id")
    @Mapping(target = "imageId", expression = "java(game.getImage() != null ? game.getImage().getId() : null)")
    @Mapping(target = "imageUrl", source="image", qualifiedByName = "buildDocumentUrlFromUserEntity") // Reusar el mismo método
    GameResponseDTO gameToGameResponseDTO(Game game);

    @Named("mapGamesToResponseDTOs")
    default List<GameResponseDTO> mapGamesToResponseDTOs(List<Game> games) {
        if (games == null) {
            return Collections.emptyList();
        }
        return games.stream()
                .filter(game -> game != null)
                .map(this::gameToGameResponseDTO) // Llama al método definido arriba
                .collect(Collectors.toList());
    }

    @Named("mapRolesToStrings")
    default List<String> mapRolesToStrings(List<Role> roles) { // Cambiado List<?> a List<Role>
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role != null && role.getName() != null)
                .map(Role::getName) // Acceso directo al nombre del rol
                .collect(Collectors.toList());
    }

    /**
     * Construye la URL relativa para un documento de perfil.
     * Si tu GameMapper tiene un método @Named("buildDocumentUrl") similar y quieres
     * reusarlo, deberías añadir GameMapper.class a la anotación @Mapper(uses = {...})
     * de esta interfaz y luego podrías usar qualifiedByName = "buildDocumentUrl" si ese
     * método es accesible y tiene la firma correcta (Document -> String).
     * Por ahora, lo defino aquí específico para UserMapper.
     */
    @Named("buildDocumentUrlFromUserEntity")
    default String buildDocumentUrlFromUserEntity(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        // Esta URL debe coincidir con un endpoint en tu DocumentController que sirva el archivo
        // ej. /api/documents/{id} o /api/documents/download/{fileName}
        // Si usas document.getFileName(), asegúrate que sea único. Usar ID es más seguro.
        return "/api/documents/" + document.getId();
    }

    // Este método está aquí pero no se usa explícitamente en toResponseDTO para profileImageUrl
    @Named("buildDocumentUrl")
    default String buildDocumentUrl(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        return "/api/documents/" + document.getId(); // O /api/documents/download/{fileName}
    }
}
