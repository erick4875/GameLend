package org.project.group5.gamelend.controller;

// DTOs para los datos de entrada de login y registro.
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

/**
 * @RestController: Controlador REST para peticiones HTTP.
 * @RequestMapping("/api/auth"): Ruta base para todos los endpoints de este controlador.
 * @RequiredArgsConstructor: Lombok crea constructor para 'authService'.
 *
 * Maneja el registro, login y refresco de tokens.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Servicio con la lógica de autenticación.
    private final AuthService authService;

    /**
     * Registra un nuevo usuario.
     * POST /api/auth/register
     * @param request Datos del nuevo usuario.
     * @return Tokens y estado 201 (CREATED).
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request) {
        TokenResponseDTO response = (TokenResponseDTO) authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Inicia sesión de un usuario.
     * POST /api/auth/login
     * @param request Credenciales (email, password).
     * @return Tokens y estado 200 (OK).
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        TokenResponseDTO response = (TokenResponseDTO) authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresca el token de acceso.
     * POST /api/auth/refresh
     * @param authHeader Cabecera "Authorization" con el token de refresco.
     * @return Nuevos tokens y estado 200 (OK).
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        TokenResponseDTO response = (TokenResponseDTO) authService.refreshToken(authHeader);
        return ResponseEntity.ok(response);
    }
}
