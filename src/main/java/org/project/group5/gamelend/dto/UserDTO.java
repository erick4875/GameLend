package org.project.group5.gamelend.dto;

import java.util.List;

import org.project.group5.gamelend.entity.Game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir y enviar datos de usuario en la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String name;
    private String publicName;
    private String email;
    private String province;
    private String city;
    private String password;
    private String registrationDate;
    
    private List<Game> games;

    private List<String> roles;
}

