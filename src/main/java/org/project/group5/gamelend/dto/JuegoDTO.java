package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.util.RespuestaGlobal.Estado;  // Importación correcta

// Clase para representar un objeto de transferencia de datos (DTO) de juego
// Se utiliza para recibir datos de juego a través de la API
// entrada de datos (cliente -> servidor)
// Crear o actualizar un juego

public class JuegoDTO {
    private String titulo;
    private String plataforma;
    private String genero;  
    private String descripcion;
    private Estado estado; 
    private Long usuarioId; 

    public JuegoDTO() {}

    // Constructor con todos los campos
    public JuegoDTO(String titulo, String plataforma, String genero, String descripcion, Estado estado, Long usuarioId) {
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.genero = genero;
        this.descripcion = descripcion;
        this.estado = estado;
        this.usuarioId = usuarioId;
    }
    
    // Getters y setters existentes...
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
    
    // Método para convertir a entidad
    public Juego toEntity() {
        Juego juego = new Juego();
        juego.setTitulo(this.titulo);
        juego.setPlataforma(this.plataforma);
        juego.setGenero(this.genero);
        juego.setDescripcion(this.descripcion);
        juego.setEstado(this.estado);
        // El usuario propietario debe ser asignado en el servicio
        return juego;
    }
}