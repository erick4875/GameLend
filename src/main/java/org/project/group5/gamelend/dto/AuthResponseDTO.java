package org.project.group5.gamelend.dto;

import java.util.List;

/**
 * DTO para devolver el token y datos básicos del usuario
 * Contiene la información mínima necesaria para el cliente después de autenticación
 *
 * @param token Token JWT de acceso
 * @param publicName Nombre público del usuario
 * @param userId ID del usuario
 * @param roles Lista de roles asignados al usuario
 */
public record AuthResponseDTO(
    String token,
    String publicName,
    Long userId,
    List<String> roles
) {}

    // Records generan automáticamente: constructor, getters, equals(), hashCode(), toString()