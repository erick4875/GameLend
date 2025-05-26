package com.example.gamelend.dto;


import java.util.ArrayList;
import java.util.List;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String publicName;
    private String email;
    private String password;
    private String registrationDate; // Recibirá el LocalDateTime como String formateado
    private String province;
    private String city;
    private List<GameResponseDTO> games;
    private List<GameSummaryDTO> gamesLent;
    private List<String> roles;

    // Constructor vacío (necesario para Gson/Moshi)
    public UserResponseDTO() {
        // Importante inicializar las listas para evitar NullPointerExceptions
        // si el JSON no incluye estos campos o los envía como null.
        this.games = new ArrayList<>();
        this.gamesLent = new ArrayList<>();
        this.roles = new ArrayList<>();
    }

    // Constructor con todos los campos
    public UserResponseDTO(Long id, String name, String publicName, String email, String registrationDate,
                           String province, String password, String city, List<GameResponseDTO> games,
                           List<GameSummaryDTO> gamesLent, List<String> roles) {
        this.id = id;
        this.name = name;
        this.publicName = publicName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.province = province;
        this.password = password;
        this.city = city;
        this.games = (games != null) ? games : new ArrayList<>(); // Asegurar no nulos
        this.gamesLent = (gamesLent != null) ? gamesLent : new ArrayList<>(); // Asegurar no nulos
        this.roles = (roles != null) ? roles : new ArrayList<>(); // Asegurar no nulos
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() { return name; }

    public String getPublicName() {
        return publicName;
    }

    public String getEmail() {
        return email;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getProvince() {
        return province;
    }

    public String getPassword() {
        return password;
    }

    public String getCity() {
        return city;
    }

    public List<GameResponseDTO> getGames() {
        return games;
    }

    public List<GameSummaryDTO> getGamesLent() {
        return gamesLent;
    }

    public List<String> getRoles() {
        return roles;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) { this.name = name; }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setPassword(String password) {
        this.province = password;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setGames(List<GameResponseDTO> games) {
        this.games = (games != null) ? games : new ArrayList<>();
    }

    public void setGamesLent(List<GameSummaryDTO> gamesLent) {
        this.gamesLent = (gamesLent != null) ? gamesLent : new ArrayList<>();
    }

    public void setRoles(List<String> roles) {
        this.roles = (roles != null) ? roles : new ArrayList<>();
    }
}