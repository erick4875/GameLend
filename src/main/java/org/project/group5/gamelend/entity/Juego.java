package org.project.group5.gamelend.entity;

import java.util.List;

import org.project.group5.gamelend.util.RespuestaGlobal.Estado;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Juego {
    @Id // clave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private long id;

    @Column(length = 50, nullable = false)
    private String titulo;

    // Nuevos campos
    @Column(length = 50)
    private String plataforma;

    @Column(length = 500) // Descripción más larga
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private Estado estado; // D = Disponible, P = Prestado

    @Column(length = 30)
    private String genero;

    // Relación uno a muchos con Prestamo
    // al eliminar un juego, todos sus prestamos son eliminados en cascada
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prestamo> prestamos;

    private String imagenRuta;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_imagen")
    private Documento imagen;

    @ManyToOne // relación de muchos a uno
    @JoinColumn(name = "id_usuario") // clave foránea
    private Usuario usuario;

    public Juego() {
    } // constructor vacio requerido por Gson

    public Juego(long id, String titulo, String plataforma, String descripcion,
            Estado estado, Usuario usuario, List<Prestamo> prestamos,
            Documento imagen) {
        this.id = id;
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.descripcion = descripcion;
        this.estado = estado;
        this.usuario = usuario;
        this.prestamos = prestamos;
        this.imagen = imagen;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public Documento getImagen() {
        return imagen;
    }

    public void setImagen(Documento imagen) {
        this.imagen = imagen;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }

    public void setImagenRuta(String imagenRuta) {
        this.imagenRuta = imagenRuta;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
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

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
