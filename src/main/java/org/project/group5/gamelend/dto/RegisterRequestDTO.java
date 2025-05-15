package org.project.group5.gamelend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de un nuevo usuario
 * Contiene los campos necesarios para crear una cuenta
 *
 * @param name          Nombre real del usuario
 * @param publicName    Nombre público/usuario
 * @param password      Contraseña
 * @param email         Correo electrónico
 * @param province      Provincia
 * @param city          Ciudad
 */
public record RegisterRequestDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String name,

        @NotBlank(message = "El nombre público no puede estar vacío")
        @Size(min = 3, max = 50, message = "El nombre público debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre público solo puede contener letras, números y guiones bajos (_)")
        String publicName,

        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        String email,

        @NotBlank(message = "La provincia no puede estar vacía")
        @Size(max = 100, message = "La provincia no puede exceder los 100 caracteres")
        String province,

        @NotBlank(message = "La ciudad no puede estar vacía")
        @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
        String city
) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}