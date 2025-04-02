package com.example.gamelend.Clases;

public class Game {
    private String nombre;
    private int imagenResource;

    public Game(String nombre, int imagenResource) {
        this.nombre = nombre;
        this.imagenResource = imagenResource;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getImagenResource() {
        return imagenResource;
    }

    public void setImagenResource(int imagenResource) {
        this.imagenResource = imagenResource;
    }
}
