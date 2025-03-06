package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String nombre;

    @Column(length = 50, unique = true, nullable = false)
    private String nombrePublico;

    @Column(nullable = false)
    private String password;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 50)
    private String provincia;

    @Column(length = 50)
    private String localidad;

    @Column(name = "fecha_registro")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;
    
    // asociación uno a muchos entre usuario y juego
    // Si un usuario es eliminado borramos todos sus juegos asociados
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Juego> juegos;
    
    // Si un usuario es eliminado, borramos todos sus prestamos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prestamo> prestamos;
    
    // Relación del usuario con prestamo como receptor
    @OneToMany(mappedBy = "usuarioReceptor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prestamo> prestamosRecibidos;

    // Relación muchos a muchos con UsuarioRol
    @ManyToMany(fetch = FetchType.EAGER) //carga los roles al mismo tiempo que el usuario
    @JoinTable(
            name = "usuario_rol_asignacion",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<UsuarioRol> roles = new HashSet<>();

    public Usuario() {} // constructor vacío requerido por JPA y útil para Gson

    public Usuario(long id, String nombre, String nombrePublico, String provincia, String localidad, String password,
                   String email, LocalDateTime fechaRegistro, List<Juego> juegos, List<Prestamo> prestamos,
                   List<Prestamo> prestamosRecibidos, Set<UsuarioRol> roles) {
        this.id = id;
        this.nombre = nombre;
        this.nombrePublico = nombrePublico;
        this.provincia = provincia;
        this.localidad = localidad;
        this.password = password;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.juegos = juegos;
        this.prestamos = prestamos;
        this.prestamosRecibidos = prestamosRecibidos;
        this.roles = roles;
    }

    // Constructor simplificado para usos comunes
    public Usuario(long id, String nombre, String nombrePublico, String provincia, String localidad, String password,
                   String email, LocalDateTime fechaRegistro, List<Juego> juegos) {
        this.id = id;
        this.nombre = nombre;
        this.nombrePublico = nombrePublico;
        this.provincia = provincia;
        this.localidad = localidad;
        this.password = password;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.juegos = juegos;
    }

    // Métodos para gestionar roles
    public void agregarRol(UsuarioRol rol) {
        roles.add(rol);
    }
    
    public void quitarRol(UsuarioRol rol) {
        roles.remove(rol);
    }
    
    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(rol -> rol.getName().equals(nombreRol));
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Juego> getJuegos() {
        return juegos;
    }

    public void setJuegos(List<Juego> juegos) {
        this.juegos = juegos;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public List<Prestamo> getPrestamosRecibidos() {
        return prestamosRecibidos;
    }

    public void setPrestamosRecibidos(List<Prestamo> prestamosRecibidos) {
        this.prestamosRecibidos = prestamosRecibidos;
    }

    public Set<UsuarioRol> getRoles() {
        return roles;
    }

    public void setRoles(Set<UsuarioRol> roles) {
        this.roles = roles;
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }   

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}

