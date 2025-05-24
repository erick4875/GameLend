package com.example.gamelend.models;

public class User {
    private String name;
    private String publicName;
    private String password;
    private String email;
    private String province;
    private String city;

    // Constructor para registrar usuario
    public User(String name, String publicName, String password, String email, String province, String city) {
        this.name = name;
        this.publicName = publicName;
        this.password = password;
        this.email = email;
        this.province = province;
        this.city = city;
    }

    // Constructor para enseñar info
    public User(String name, String publicName, String email, String province, String city) {
        this.name = name;
        this.publicName = publicName;
        this.email = email;
        this.province = province;
        this.city = city;
    }


    // Getters y setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
