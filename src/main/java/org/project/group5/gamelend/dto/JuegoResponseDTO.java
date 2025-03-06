package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.util.RespuestaGlobal.Estado;

// Clase para representar un objeto de transferencia de datos (DTO) de respuesta de juego
// Se utiliza para enviar datos de juego a través de la API
// Salida de datos (servidor -> cliente)
// Mostrar información detallada de un juego

public class JuegoResponseDTO {
    private Long id;
    private String titulo;
    private String plataforma;
    private String genero;
    private String descripcion;
    private Estado estado;
    private Long usuarioId;
    private String usuarioNombre;
    
    // Constructor vacío
    public JuegoResponseDTO() {}
    
    // Método de conversión de entidad a DTO de respuesta
    public static JuegoResponseDTO fromEntity(Juego juego) {
        JuegoResponseDTO dto = new JuegoResponseDTO();
        dto.setId(juego.getId());
        dto.setTitulo(juego.getTitulo());
        dto.setPlataforma(juego.getPlataforma());
        dto.setGenero(juego.getGenero());
        dto.setDescripcion(juego.getDescripcion());
        dto.setEstado(juego.getEstado());
        
        if (juego.getUsuario() != null) {
            dto.setUsuarioId(juego.getUsuario().getId());
            dto.setUsuarioNombre(juego.getUsuario().getNombrePublico());
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
}
