package org.project.group5.gamelend.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;

@Mapper(componentModel = "spring", uses = GameMapper.class)
public interface UserMapper {
    
    // ====== MÉTODOS DE ENTITY A DTO =======
    
    /**
     * Convierte una entidad User a UserResponseDTO
     */
    @Mapping(target = "registrationDate", source = "registrationDate", qualifiedByName = "userFormatDateTime")
    @Mapping(target = "games", source = "games")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "gamesLent", expression = "java(getGamesLent(user))")
    UserResponseDTO toResponseDTO(User user);
    
    /**
     * Convierte una lista de User a lista de UserResponseDTO
     */
    List<UserResponseDTO> toResponseDTOList(List<User> users);
    
    /**
     * Convierte una entidad User a UserSummaryDTO
     */
    UserSummaryDTO toSummaryDTO(User user);
    
    /**
     * Convierte una lista de User a lista de UserSummaryDTO
     */
    List<UserSummaryDTO> toSummaryDTOList(List<User> users);
    
    // ====== MÉTODOS DE DTO A ENTITY =======
    
    /**
     * Convierte UserDTO a entidad User (para creación)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", expression = "java(parseDateTime(dto.getRegistrationDate()))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
    @Mapping(target = "loansMade", ignore = true)
    @Mapping(target = "loansReceived", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User toEntity(UserDTO dto);
    
    /**
     * Actualiza una entidad User con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "games", ignore = true)
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
     * Convierte una fecha String a LocalDateTime
     */
    default LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    /**
     * Convierte roles a lista de strings
     */
    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene juegos prestados por el usuario
     */
    @Named("getGamesLent")
    default List<GameSummaryDTO> getGamesLent(User user) {
        if (user.getLoansMade() == null) {
            return List.of();
        }
        
        return user.getLoansMade().stream()
                .filter(loan -> loan.getReturnDate() == null) // Solo préstamos activos
                .map(loan -> loan.getGame())
                .map(game -> {
                    return GameSummaryDTO.builder()
                            .id(game.getId())
                            .title(game.getTitle())
                            .platform(game.getPlatform())
                            .status(game.getStatus() != null ? game.getStatus().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
