package com.example.gamelend.dto;

import java.util.List;

public class UserDTO {
    private String name;
    private String publicName;
    private String email;
    private String province;
    private String city;
    private String password;
    private String registrationDate; // Usamos String para recibir el LocalDateTime
    private List<GameDTO> games;
    private List<String> roles;

    public UserDTO() {}

    // Constructor
    public UserDTO(String name, String publicName, String email, String province,
                   String city, String password, String registrationDate,
                   List<GameDTO> games, List<String> roles) {
        this.name = name;
        this.publicName = publicName;
        this.email = email;
        this.province = province;
        this.city = city;
        this.password = password;
        this.registrationDate = registrationDate;
        this.games = games;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<GameDTO> getGames() {
        return games;
    }

    public void setGames(List<GameDTO> games) {
        this.games = games;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}