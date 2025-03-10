package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Usuario;

// Clase para representar un objeto de transferencia de datos (DTO) de resumen de usuario
// Se utiliza para enviar datos de usuario a través de la API
// salida de datos (servidor -> cliente)

public class UsuarioResumenDTO {
    private Long id;
    private String nombrePublico;

    public UsuarioResumenDTO() {
    }

    public UsuarioResumenDTO(Long id, String nombrePublico) {
        this.id = id;
        this.nombrePublico = nombrePublico;
    }

    public static UsuarioResumenDTO fromEntity(Usuario usuario) {
        if (usuario == null)
            return null;
        return new UsuarioResumenDTO(usuario.getId(), usuario.getNombrePublico());
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

    @Override
    public String toString() {
        return "UsuarioResumenDTO{" +
                "id=" + id +
                ", nombrePublico='" + nombrePublico + '\'' +
                '}';
    }
}
