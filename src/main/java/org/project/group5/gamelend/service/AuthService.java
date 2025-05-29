package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Necesario para Collectors.toList()

import org.project.group5.gamelend.dto.LoginRequestDTO;
import org.project.group5.gamelend.dto.RegisterRequestDTO;
import org.project.group5.gamelend.dto.TokenResponseDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.Token;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.TokenRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Asegúrate que esta importación esté si la usas
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la autenticación y gestión de tokens.
 * Maneja registro, login y renovación de tokens JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // === Dependencias inyectadas ===
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    // === Operaciones de Autenticación ===

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param request Datos de registro (nombre, email, contraseña, etc)
     * @return TokenResponseDTO con tokens y datos del usuario
     * @throws ResponseStatusException si el email o nombre público ya existen
     */
    @Transactional
    public TokenResponseDTO register(RegisterRequestDTO request) {
        // Verifica si el email ya está en uso.
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        // Verifica si el nombre público ya está en uso.
        if (userRepository.existsByPublicName(request.publicName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre público ya está en uso");
        }

        // Crea el nuevo usuario.
        var user = User.builder()
                .name(request.name())
                .publicName(request.publicName())
                .password(passwordEncoder.encode(request.password())) // Codifica la contraseña.
                .email(request.email())
                .province(request.province())
                .city(request.city())
                .registrationDate(LocalDateTime.now())
                .build();

        // Asigna el rol por defecto "ROLE_USER".
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("CRITICAL: El rol por defecto ROLE_USER no se encuentra en la base de datos.");
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Rol por defecto no encontrado");
                });
        user.addRole(userRole); // Asegúrate que tu entidad User tenga el método addRole y la colección roles
                                // inicializada

        var savedUser = userRepository.save(user);
        log.info("Usuario registrado: {}", savedUser.getEmail());

        var jwtAccessToken = jwtService.generateToken(savedUser); // Genera access token
        var jwtRefreshToken = jwtService.generateRefreshToken(savedUser); // Genera refresh token

        saveUserToken(savedUser, jwtAccessToken);

        // Preparar la lista de nombres de roles para el DTO de respuesta
        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName) // Asumiendo que Role tiene un método getName()
                .collect(Collectors.toList());

        return new TokenResponseDTO(jwtAccessToken, jwtRefreshToken, savedUser.getId(), savedUser.getPublicName(),
                user.getEmail(), roleNames);
    }

    /**
     * Autentica un usuario existente.
     * 
     * @param request Credenciales de login (email y contraseña)
     * @return TokenResponseDTO con nuevos tokens y datos del usuario
     * @throws BadCredentialsException si las credenciales son inválidas
     */
    @Transactional
    public TokenResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            log.warn("Intento de login fallido para email {}: {}", request.email(), e.getMessage());
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Si la autenticación es exitosa, cargar el usuario para obtener sus datos
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Usuario autenticado {} no encontrado en la base de datos después del login.",
                            request.email());
                    return new UsernameNotFoundException("Usuario no encontrado después del login: " + request.email());
                });
        log.info("Usuario logueado: {}", user.getEmail());

        var jwtAccessToken = jwtService.generateToken(user);
        var jwtRefreshToken = jwtService.generateRefreshToken(user);

        // Revocar tokens de acceso anteriores y guardar el nuevo
        revokeAllUserTokens(user); // Esto debería revocar los ACCESS tokens.
        saveUserToken(user, jwtAccessToken); // Guarda el nuevo ACCESS token.

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new TokenResponseDTO(jwtAccessToken, jwtRefreshToken, user.getId(), user.getPublicName(),
                user.getEmail(), roleNames);
    }

    /**
     * Renueva el token de acceso usando un refresh token.
     * 
     * @param authHeader Header con formato "Bearer <refreshToken>"
     * @return TokenResponseDTO con nuevo access token
     * @throws ResponseStatusException si el token es inválido o expiró
     */
    @Transactional
    public TokenResponseDTO refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Intento de refresco con cabecera Authorization inválida o ausente.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato de token inválido en cabecera Authorization.");
        }

        final String tokenString = authHeader.substring(7); // Extrae el token

        String tokenType = jwtService.extractTokenType(tokenString);
        // Usa la constante pública de JwtService si la tienes
        if (!"refresh".equals(tokenType)) {
            log.warn("Intento de usar un token de tipo '{}' para refrescar, se esperaba 'refresh'.", tokenType);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token proporcionado no es un token de actualización.");
        }

        final String userEmail = jwtService.extractUsername(tokenString);
        if (userEmail == null) {
            log.warn("No se pudo extraer email del token de actualización.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de actualización inválido (sin email).");
        }

        final User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para email '{}' durante refresco de token.", userEmail);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de actualización inválido.");
                });

        // Asegúrate que tu entidad User implementa UserDetails para esta llamada.
        if (!jwtService.isRefreshTokenValid(tokenString, user)) {
            log.warn("Validación fallida para el token de actualización del usuario: {}", userEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de actualización inválido o expirado.");
        }

        log.info("Refrescando token para usuario: {}", userEmail);
        final String newAccessToken = jwtService.generateToken(user); // Genera nuevo access token

        revokeAllUserTokens(user); // Revoca los ACCESS tokens
        saveUserToken(user, newAccessToken); // Guarda el nuevo ACCESS token

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return new TokenResponseDTO(newAccessToken, tokenString, user.getId(), user.getPublicName(), user.getEmail(),
                roleNames);
    }

    // === Gestión de Tokens ===

    /**
     * Guarda un nuevo token de acceso.
     * 
     * @param user           Usuario propietario del token
     * @param jwtAccessToken Token JWT a guardar
     */
    private void saveUserToken(User user, String jwtAccessToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtAccessToken) // Guardando el access token
                .tokenType(Token.TokenType.BEARER) // Podrías tener TokenType.ACCESS y TokenType.REFRESH si guardas
                                                   // ambos
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
        log.debug("Token guardado para el usuario {}: {}", user.getEmail(), jwtAccessToken.substring(0, 10) + "...");
    }

    /**
     * Revoca todos los tokens activos de un usuario.
     * Útil al cambiar contraseña o cerrar sesión.
     * 
     * @param user Usuario cuyos tokens serán revocados
     */
    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            log.debug("Revocando {} token(s) para el usuario {}", validUserTokens.size(), user.getEmail());
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    // === Utilidades Privadas ===

    /**
     * Valida credenciales con Spring Security.
     * 
     * @throws AuthenticationException si las credenciales son inválidas
     */
    private void validateCredentials(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    /**
     * Verifica duplicados de email y nombre público.
     * 
     * @throws ResponseStatusException si hay conflictos
     */
    private void checkDuplicates(String email, String publicName) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso");
        }
        if (userRepository.existsByPublicName(publicName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre público ya está en uso");
        }
    }
}