package org.project.group5.gamelend.controller;

import org.project.group5.gamelend.dto.LoginRequestDTO;
import org.project.group5.gamelend.dto.RegisterRequestDTO;
import org.project.group5.gamelend.dto.TokenResponseDTO;
import org.project.group5.gamelend.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la autenticación de usuarios.
 * Gestiona registro, login y renovación de tokens.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param request datos del registro (nombre, email, contraseña, etc)
     * @return tokens de autenticación
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request) {
        log.info("Nuevo registro de usuario: {}", request.email());
        TokenResponseDTO response = (TokenResponseDTO) authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica un usuario existente.
     * 
     * @param request credenciales de login (email y contraseña)
     * @return tokens de autenticación
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        log.info("Intento de login: {}", request.email());
        TokenResponseDTO response = (TokenResponseDTO) authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el token de acceso usando el token de refresco.
     * 
     * @param authHeader token de refresco en header Authorization
     * @return nuevos tokens de autenticación
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        log.debug("Solicitud de renovación de token");
        TokenResponseDTO response = (TokenResponseDTO) authService.refreshToken(authHeader);
        return ResponseEntity.ok(response);
    }
}
