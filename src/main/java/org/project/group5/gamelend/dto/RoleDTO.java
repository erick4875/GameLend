package org.project.group5.gamelend.dto;

/**
 * DTO para representar un Rol
 *
 * @param idRole ID del rol
 * @param name Nombre del rol
 */
public record RoleDTO(
    Long idRole,
    String name
) {
    // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()

    // Constructor completo para validar el nombre del rol
    public RoleDTO {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede ser nulo o vacío");
        }
    }
    
}
