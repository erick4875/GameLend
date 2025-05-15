package org.project.group5.gamelend.mapper;

import java.util.List;
import java.util.stream.Collectors; // Added for stream operations

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Convierte una entidad Role a RoleDTO.
     * Asume que los nombres de los campos (idRole, name) coinciden entre Role y RoleDTO.
     * Si el campo ID en la entidad Role se llama 'id' en lugar de 'idRole',
     * necesitarías: @Mapping(source = "id", target = "idRole")
     * @param role La entidad Role.
     * @return El RoleDTO correspondiente.
     */
    RoleDTO toDTO(Role role);

    /**
     * Convierte un RoleDTO a una entidad Role.
     * Asume que los nombres de los campos (idRole, name) coinciden.
     * Si el campo ID en la entidad Role se llama 'id' en lugar de 'idRole',
     * necesitarías: @Mapping(source = "idRole", target = "id")
     * @param dto El RoleDTO.
     * @return La entidad Role correspondiente.
     */
    Role toEntity(RoleDTO dto);

    /**
     * Convierte una lista de entidades Role a una lista de RoleDTO.
     * Esta es ahora una implementación default para evitar problemas de generación de MapStruct.
     * @param roles Lista de entidades Role.
     * @return Lista de RoleDTO.
     */
    default List<RoleDTO> toDTOList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Convierte una lista de entidades Role a una lista de sus nombres (String).
     * Esta es ahora una implementación default que llama al método roleToName.
     * @param roles Lista de entidades Role.
     * @return Lista de nombres de roles.
     */
    // @IterableMapping(qualifiedByName = "roleToName") // No longer needed by MapStruct for this method as it's now default
    default List<String> toRoleNameList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(this::roleToName).collect(Collectors.toList());
    }

    /**
     * Método calificado por nombre que convierte una entidad Role a su nombre (String).
     * Este método es utilizado por MapStruct cuando se especifica mediante @Named("roleToName").
     * @param role La entidad Role.
     * @return El nombre del rol, o null si el rol es null.
     */
    @Named("roleToName")
    default String roleToName(Role role) {
        return role != null ? role.getName() : null;
    }
}