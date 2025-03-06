package org.project.group5.gamelend.entity;

import jakarta.persistence.*;

@Entity
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // La base de datos generará el ID autoincrementado
    private long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String fileName;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false, length = 1)
    private String estado;

    @Column(nullable = false)
    private boolean eliminado;

    @Lob
    private byte[] imagen;

    @Column(length = 255)
    private String rutaLocal; // Ruta donde se guarda la imagen en local

    @Transient // No se almacena en BD, uso temporal para respuesta JSON
    private String urlFile;

    public Documento() {
        this.id = 0;
        this.nombre = "";
        this.fileName = "";
        this.extension = "";
        this.estado = "D";
        this.eliminado = false;
    }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public String getCompleteFileName() {
        return fileName + extension;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

}
