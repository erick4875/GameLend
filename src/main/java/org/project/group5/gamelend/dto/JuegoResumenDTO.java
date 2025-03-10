package org.project.group5.gamelend.dto;

import org.project.group5.gamelend.entity.Juego;

/**
 * DTO simplificado de Juego con información mínima
 */

// Preparado para listas o como referencia en otros DTOs

public class JuegoResumenDTO {
    private Long id;
    private String titulo;
    private String plataforma;
    private String estado;
    
    public JuegoResumenDTO() {}
    
    public JuegoResumenDTO(Long id, String titulo, String plataforma, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.estado = estado;
    }
    
    public static JuegoResumenDTO fromEntity(Juego juego) {
        if (juego == null) return null;
        return new JuegoResumenDTO(
            juego.getId(), 
            juego.getTitulo(), 
            juego.getPlataforma(), 
            juego.getEstado() != null ? juego.getEstado().name() : null
        );
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}