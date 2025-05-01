package org.project.group5.gamelend.dto;

import java.util.List;

/**
 * DTO para devolver el token y datos básicos del usuario.
 * Contiene la información mínima necesaria para el cliente después de autenticación.
 */
public record AuthResponse(
    String token,
    String nombrePublico,
    Long userId,
    List<String> roles
) {}