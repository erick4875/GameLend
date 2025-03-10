package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.entity.UsuarioRol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clase para representar un objeto de transferencia de datos (DTO) de respuesta de usuario
// Se utiliza para enviar datos de usuario a través de la API
// salida de datos (servidor -> cliente)

public class UsuarioResponseDTO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioResponseDTO.class);
    
    private Long id;
    private String nombrePublico;
    private String email;
    private String fechaRegistro;
    private String provincia;
    private String localidad;
    private List<JuegoResponseDTO> juegos;
    private List<String> roles; // Mantenemos como List<String> para la respuesta JSON

    public UsuarioResponseDTO() {
    }

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombrePublico(usuario.getNombrePublico());
        dto.setEmail(usuario.getEmail());
        
        // Usar el nuevo método setFechaRegistro que acepta LocalDateTime
        dto.setFechaRegistro(usuario.getFechaRegistro());
        
        dto.setProvincia(usuario.getProvincia());
        dto.setLocalidad(usuario.getLocalidad());
        
        // Reemplazar System.out.println por logger
        logger.debug("Roles del usuario: {}", usuario.getRoles());
        
        // Verificar explícitamente los roles
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            List<String> roleNames = new ArrayList<>();
            for (UsuarioRol rol : usuario.getRoles()) {
                if (rol != null && rol.getName() != null) {
                    roleNames.add(rol.getName());
                    logger.debug("Añadido rol: {}", rol.getName());
                }
            }
            dto.setRoles(roleNames);
        } else {
            logger.debug("No se encontraron roles para el usuario");
            dto.setRoles(new ArrayList<>());
        }

        return dto;
    }

    // Método para crear DTOs completos con colecciones
    public static UsuarioResponseDTO fromEntityCompleto(Usuario usuario) {
        UsuarioResponseDTO dto = fromEntity(usuario); // Obtener DTO básico con los roles

        // Acceder a colecciones solo si ya están cargadas (dentro de una transacción)
        if (Hibernate.isInitialized(usuario.getJuegos())) {
            dto.setJuegos(usuario.getJuegos().stream()
                    .map(JuegoResponseDTO::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            dto.setJuegos(new ArrayList<>());
        }

        return dto;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    // 1. Modifica el método getFechaRegistro
    public String getFechaRegistro() {
        return fechaRegistro; // Simplemente devuelve el String, no necesita formato
    }

    // 2. Modifica el método setFechaRegistro para aceptar LocalDateTime
    public void setFechaRegistro(LocalDateTime fechaDateTime) {
        if (fechaDateTime == null) {
            this.fechaRegistro = null;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.fechaRegistro = fechaDateTime.format(formatter);
        }
    }

    // 2b. También mantén el método original para aceptar String
    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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

    public List<JuegoResponseDTO> getJuegos() {
        return juegos;
    }

    public void setJuegos(List<JuegoResponseDTO> juegos) {
        this.juegos = juegos;
    }

    // Getters y setters para roles
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
