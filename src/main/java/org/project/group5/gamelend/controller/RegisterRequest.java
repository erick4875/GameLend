package org.project.group5.gamelend.controller;

/**
 * Registro (record) para encapsular los datos de solicitud de registro de usuario
 * 
 * Esta clase inmutable contiene todos los campos necesarios para crear una nueva cuenta de usuario.
 */
public record RegisterRequest(
    String name,
    String publicName,
    String password,
    String email,
    String province,
    String city
) {
}
