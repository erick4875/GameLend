package com.example.gamelend.Clases;

public class Usuario {
    private String usuario , contrasena,nombre,ciudad;

    public Usuario(String usuario, String contrasena, String nombre, String ciudad){
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.ciudad = ciudad;
    }

    public String getUsuario(){
        return usuario;
    }

    public String getContrasena(){
        return contrasena;
    }

    public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }

    public String getNombre(){
        return nombre;
    }

    public String getCiudad(){
        return ciudad;
    }

}
