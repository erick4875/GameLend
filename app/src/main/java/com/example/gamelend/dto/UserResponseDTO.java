package com.example.gamelend.dto;

// import com.google.gson.annotations.SerializedName; // Solo si el nombre JSON es diferente al del campo
import java.util.List;
import java.util.ArrayList;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String publicName;
    private String email;
    private String registrationDate;
    private String province;
    private String city;
    private String phone;
    private String profileImageUrl; // <--- AÑADIDO CAMPO PARA URL DE IMAGEN DE PERFIL
    private List<GameResponseDTO> games;       // Asume que este DTO existe en Android
    private List<GameSummaryDTO> gamesLent; // Asume que este DTO existe en Android
    private List<String> roles;

    // Constructor vacío
    public UserResponseDTO() {
        this.games = new ArrayList<>();
        this.gamesLent = new ArrayList<>();
        this.roles = new ArrayList<>();
    }

    // Constructor completo (opcional, pero útil para tests o creación manual)
    public UserResponseDTO(Long id, String name, String publicName, String email, String registrationDate,
                           String province, String city, String phone, String profileImageUrl,
                           List<GameResponseDTO> games, List<GameSummaryDTO> gamesLent, List<String> roles) {
        this.id = id;
        this.name = name;
        this.publicName = publicName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.province = province;
        this.city = city;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl; // <--- AÑADIDO AL CONSTRUCTOR
        this.games = (games != null) ? games : new ArrayList<>();
        this.gamesLent = (gamesLent != null) ? gamesLent : new ArrayList<>();
        this.roles = (roles != null) ? roles : new ArrayList<>();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublicName() { return publicName; }
    public void setPublicName(String publicName) { this.publicName = publicName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImageUrl() { return profileImageUrl; } // <--- GETTER PARA PROFILE IMAGE URL
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; } // <--- SETTER

    public List<GameResponseDTO> getGames() { return games; }
    public void setGames(List<GameResponseDTO> games) { this.games = (games != null) ? games : new ArrayList<>(); }

    public List<GameSummaryDTO> getGamesLent() { return gamesLent; }
    public void setGamesLent(List<GameSummaryDTO> gamesLent) { this.gamesLent = (gamesLent != null) ? gamesLent : new ArrayList<>(); }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = (roles != null) ? roles : new ArrayList<>(); }
}
