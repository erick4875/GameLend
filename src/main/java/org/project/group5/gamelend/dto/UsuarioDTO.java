package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.entity.Usuario;


// Clase para representar un objeto de transferencia de datos (DTO) de usuario
// Se utiliza para recibir datos de usuario a través de la API
// entrada de datos (cliente -> servidor)

public class UsuarioDTO {
    private String nombre;
    private String nombrePublico;
    private String email;
    private String provincia;
    private String localidad;
    private String password;
    private String fechaRegistro;
    private List<Juego> juegos;

    // Constructor vacío necesario para deserialización JSON
    public UsuarioDTO() {
    }

    public UsuarioDTO(String nombrePublico, String email, String fechaRegistro) {
        this.nombrePublico = nombrePublico;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
    }

    public UsuarioDTO(String nombrePublico, String fechaRegistro) {
        this.nombrePublico = nombrePublico;
        this.fechaRegistro = fechaRegistro;
    }

    public UsuarioDTO(String nombrePublico, List<Juego> juegos) {
        this.nombrePublico = nombrePublico;
        this.juegos = juegos;
    }

    // Constructor completo
    public UsuarioDTO(String nombrePublico, String email, String fechaRegistro, List<Juego> juegos) {
        this.nombrePublico = nombrePublico;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.juegos = juegos;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

    public void setNombrePublico(String nombrePublico) {
        this.nombrePublico = nombrePublico;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Juego> getJuegos() {
        return juegos;
    }

    public void setJuegos(List<Juego> juegos) {
        this.juegos = juegos;
    }

    // Método para convertir a entidad
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setNombrePublico(nombrePublico);
        usuario.setEmail(email);
        usuario.setProvincia(provincia);
        usuario.setLocalidad(localidad);
        usuario.setPassword(password);
        usuario.setFechaRegistro(LocalDateTime.now());
        return usuario;
    }

    // En un método de mapeo DTO
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        if (usuario == null) return null;
        
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        // otros campos...
        
        // Convertir LocalDateTime a String en formato ISO
        LocalDateTime fechaRegistro = usuario.getFechaRegistro();
        if (fechaRegistro != null) {
            dto.setFechaRegistro(fechaRegistro.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        
        return dto;
    }

    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "nombrePublico='" + nombrePublico + '\'' +
                ", email='" + email + '\'' +
                ", fechaRegistro='" + fechaRegistro + '\'' +
                ", juegos=" + (juegos != null ? juegos.size() : 0) + 
                '}';
    }
}
