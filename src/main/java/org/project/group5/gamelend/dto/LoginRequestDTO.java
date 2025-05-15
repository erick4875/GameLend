package org.project.group5.gamelend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de inicio de sesión
 * Contiene las credenciales para autenticar a un usuario
 *
 * @param email    Correo electrónico del usuario (requerido, formato email)
 * @param password Contraseña del usuario (requerida)
 */
public record LoginRequestDTO(
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
        // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()
}