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

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user") // nombre en la base de datos
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, unique = true, nullable = false)
    private String publicName;

    @Column(nullable = false)
    private String password;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 50)
    private String province;

    @Column(length = 50)
    private String city;

    @Column(name = "registration_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games;

    @OneToMany(mappedBy = "lender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loansMade;

    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loansReceived;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id_user"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id_role"))
    private List<Role> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Token> tokens;

    /**
     * Añade un rol al usuario si no lo tiene ya
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
     * Elimina un rol del usuario
     */
    public void removeRole(Role role) {
        if (role != null && roles != null) {
            roles.removeIf(r -> r.getIdRole().equals(role.getIdRole()));
        }
    }

}
