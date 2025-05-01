package org.project.group5.gamelend.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    // Métodos para elementos individuales
    RoleDTO toDto(Role role);

    Role toEntity(RoleDTO dto);

    // Implementación manual de métodos de colección
    default List<RoleDTO> toDtoList(List<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        List<RoleDTO> dtoList = new ArrayList<>(roles.size());
        for (Role role : roles) {
            dtoList.add(toDto(role));
        }
        return dtoList;
    }

    default List<String> toRoleNameList(List<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        List<String> nameList = new ArrayList<>(roles.size());
        for (Role role : roles) {
            nameList.add(roleToString(role));
        }
        return nameList;
    }

    /**
     * Método auxiliar que indica a MapStruct cómo convertir un Role a String
     */
    default String roleToString(Role role) {
        return role != null ? role.getName() : null;
    }
}