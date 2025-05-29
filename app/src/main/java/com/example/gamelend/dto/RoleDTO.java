package com.example.gamelend.dto;

/**
 * DTO para representar un Rol en la app Android.
 */
public class RoleDTO {

    private Long idRole;
    private String name;

    // Constructor vacío
    public RoleDTO() {
    }

    // Constructor con todos los campos
    public RoleDTO(Long idRole, String name) {
        this.idRole = idRole;
        this.name = name;
    }

    // Getters
    public Long getIdRole() {
        return idRole;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public void setName(String name) {
        this.name = name;
    }
}
