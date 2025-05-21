package org.project.group5.gamelend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Convierte una entidad Role a RoleDTO.
     */
    RoleDTO toDTO(Role role);

    /**
     * Convierte un RoleDTO a una entidad Role.
     */
    Role toEntity(RoleDTO dto);

    /**
     * Convierte una lista de entidades Role a una lista de RoleDTO.
     */
    default List<RoleDTO> toDTOList(List<Role> roles) {
        if (roles == null)
            return null;
        return roles.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Convierte una lista de entidades Role a una lista de nombres de rol.
     */
    default List<String> toRoleNameList(List<Role> roles) {
        if (roles == null)
            return null;
        return roles.stream().map(this::roleToName).collect(Collectors.toList());
    }

    /**
     * Devuelve el nombre del rol o null si el rol es null.
     */
    @Named("roleToName")
    default String roleToName(Role role) {
        return role != null ? role.getName() : null;
    }

    /**
     * Convierte una lista de entidades Role a una lista de nombres de rol (String).
     * Elimina el prefijo "ROLE_" de cada nombre.
     *
     * @param roles Lista de entidades Role
     * @return Lista de nombres de rol sin el prefijo "ROLE_"
     */
    @Named("mapRolesToStrings")
    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null)
            return Collections.emptyList();
        return roles.stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toCollection(java.util.LinkedList::new));
    }

    
}