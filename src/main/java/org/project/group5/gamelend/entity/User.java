package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un usuario en el sistema.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user") 
    private Long id;

    /**
     * Nombre real del usuario.
     */
    @Column(length = 50, nullable = false)
    private String name;

    /**
     * Nombre público y único del usuario.
     */
    @Column(length = 50, unique = true, nullable = false)
    private String publicName;

    /**
     * Contraseña del usuario (hasheada).
     */
    @Column(nullable = false)
    private String password;

    /**
     * Correo electrónico único del usuario.
     */
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    /**
     * Provincia del usuario.
     */
    @Column(length = 50)
    private String province;

    /**
     * Ciudad del usuario.
     */
    @Column(length = 50)
    private String city;

    /**
     * Fecha y hora de registro del usuario.
     */
    @Column(name = "registration_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    /**
     * Lista de juegos que posee el usuario.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games;

    /**
     * Lista de préstamos donde el usuario es el prestamista.
     */
    @OneToMany(mappedBy = "lender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loansMade;

    /**
     * Lista de préstamos donde el usuario es el prestatario.
     */
    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loansReceived;

    /**
     * Lista de roles asignados al usuario.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
               joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id_user"), 
               inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id_role"))
    private List<Role> roles;

    /**
     * Lista de tokens de autenticación asociados al usuario.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Token> tokens;

    /**
     * Añade un rol al usuario si no lo tiene ya.
     * @param role El rol a añadir.
     */
    public void addRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new ArrayList<>();
            }
            // Verifica si ya existe el rol antes de añadirlo
            if (this.roles.stream().noneMatch(r -> r.getIdRole().equals(role.getIdRole()))) {
                this.roles.add(role);
            }
        }
    }

    /**
     * Elimina un rol del usuario.
     * @param role El rol a eliminar.
     */
    public void removeRole(Role role) {
        if (role != null && roles != null) {
            roles.removeIf(r -> r.getIdRole().equals(role.getIdRole()));
        }
    }

}
