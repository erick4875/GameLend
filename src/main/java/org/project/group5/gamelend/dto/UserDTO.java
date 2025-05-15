package org.project.group5.gamelend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat; 

/**
 * DTO para datos de usuario (entrada/salida)
 * Usado para crear, actualizar o mostrar información de usuarios
 *
 * @param name             Nombre real del usuario
 * @param publicName       Nombre público/identificador único
 * @param email            Correo electrónico
 * @param province         Provincia
 * @param city             Ciudad
 * @param password         Contraseña (usado principalmente para creación/actualización)
 * @param registrationDate Fecha de registro
 * @param games            Lista de juegos asociados
 * @param roles            Lista de nombres de roles asignados
 */
public record UserDTO(
    String name,
    String publicName,
    String email,
    String province,
    String city,
    String password,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") 
    LocalDateTime registrationDate,
    List<GameDTO> games,
    List<String> roles
) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}

