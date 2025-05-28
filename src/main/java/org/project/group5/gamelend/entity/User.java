package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection; // Para las autoridades
import java.util.List;
import java.util.stream.Collectors; // Para mapear roles a GrantedAuthority

import org.springframework.security.core.GrantedAuthority; // Importante
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importante
import org.springframework.security.core.userdetails.UserDetails; // Importante

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
import jakarta.persistence.OneToOne;
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
public class User implements UserDetails {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true) 
    @JoinColumn(name = "profile_image_id", referencedColumnName = "id")
    private Document profileImage;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Esta anotación es para serialización JSON, no afecta a UserDetails
    private LocalDateTime registrationDate;

    /**
     * Lista de juegos que posee el usuario.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Si usas Lombok Builder para inicializar colecciones
    private List<Game> games = new ArrayList<>();

    /**
     * Lista de préstamos donde el usuario es el prestamista.
     */
    @OneToMany(mappedBy = "lender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Loan> loansMade = new ArrayList<>();

    /**
     * Lista de préstamos donde el usuario es el prestatario.
     */
    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Loan> loansReceived = new ArrayList<>();

    /**
     * Lista de roles asignados al usuario.
     * FetchType.EAGER es importante para que los roles se carguen con el usuario
     * cuando Spring Security lo necesite.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id_user"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id_role") // Asegúrate
                                                                                                                                                                                                        // que
                                                                                                                                                                                                        // Role
                                                                                                                                                                                                        // tenga
                                                                                                                                                                                                        // id_role
    )
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    /**
     * Lista de tokens de autenticación asociados al usuario.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) // orphanRemoval
                                                                                                           // y
                                                                                                           // CascadeType.ALL
                                                                                                           // para
                                                                                                           // tokens
    @Builder.Default
    private List<Token> tokens = new ArrayList<>();

    // --- MÉTODOS REQUERIDOS POR UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si 'roles' es null o vacío, devuelve una lista vacía de autoridades.
        if (this.roles == null || this.roles.isEmpty()) {
            return List.of(); // O java.util.Collections.emptyList();
        }
        // Mapea tus entidades Role a SimpleGrantedAuthority.
        // Asume que tu entidad Role tiene un método getName() que devuelve el nombre
        // del rol (ej. "ROLE_USER").
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password; // Devuelve el campo de tu contraseña hasheada.
    }

    @Override
    public String getUsername() {
        return this.email; // Usa el email como el "username" para Spring Security.
                           // Podrías usar publicName si es único y lo prefieres, pero el email es común.
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // O implementa lógica si manejas expiración de cuentas.
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // O implementa lógica si manejas bloqueo de cuentas.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // O implementa lógica si manejas expiración de credenciales.
    }

    @Override
    public boolean isEnabled() {
        return true; // O implementa lógica si los usuarios pueden ser deshabilitados.
    }

    // --- Métodos de utilidad que ya tenías ---
    /**
     * Añade un rol al usuario si no lo tiene ya.
     * 
     * @param role El rol a añadir.
     */
    public void addRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new ArrayList<>();
            }
            // Verifica si ya existe el rol antes de añadirlo (basado en ID o nombre)
            // Asumiendo que Role tiene un getIdRole() o un getName() único
            if (this.roles.stream().noneMatch(r -> r.getIdRole().equals(role.getIdRole()))) {
                this.roles.add(role);
            }
        }
    }

    /**
     * Elimina un rol del usuario.
     * 
     * @param role El rol a eliminar.
     */
    public void removeRole(Role role) {
        if (role != null && this.roles != null) {
            // Asumiendo que Role tiene un getIdRole()
            this.roles.removeIf(r -> r.getIdRole().equals(role.getIdRole()));
        }
    }
}