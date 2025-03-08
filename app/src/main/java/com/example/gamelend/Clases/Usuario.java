package com.example.gamelend.Clases;

public class Usuario {
    private String nombre;
    private String ciudad;
    private int imagen;
    private int juegos;
    public Usuario(String nombre, String ciudad, int imagen, int juegos){
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.imagen = imagen;
        this.juegos = juegos;
        }
    public String getNombre(){
        return nombre;
    }
    public String getCiudad(){
        return ciudad;
    }
    public int getImagen(){
        return imagen;
    }
    public int getJuegos(){
        return juegos;
    }

}
