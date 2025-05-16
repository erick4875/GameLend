package com.example.gamelend.dto;


public class UserResponseDTO {

    private Long id;
    private String nombrePublico;
    private String email;
    private String provincia;
    private String localidad;
    private String fechaRegistro;

    public UserResponseDTO(Long id, String nombrePublico, String email, String provincia,
                           String localidad, String fechaRegistro) {
        this.id = id;
        this.nombrePublico = nombrePublico;
        this.email = email;
        this.provincia = provincia;
        this.localidad = localidad;
        this.fechaRegistro = fechaRegistro;
    }

    public UserResponseDTO() {}

    public Long getId(){
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

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}