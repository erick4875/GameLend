package org.project.group5.gamelend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Rol: representa un rol de usuario (ADMIN o USER).
 */
@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * ID único del rol (autogenerado)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    /** 
     * Nombre único del rol (ej. "ROLE_ADMIN", "ROLE_USER")
     * Es obligatorio y no se puede repetir
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}