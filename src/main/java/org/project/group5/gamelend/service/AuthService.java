package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la autenticación y registro de usuarios.
 */
@Service
@RequiredArgsConstructor // Inyección de dependencias por constructor (Lombok).
@Slf4j // Logger (Lombok).
public class AuthService {

    // Repositorios y servicios necesarios.
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    /**
     * Registra un nuevo usuario en el sistema.
     * @param request Datos de registro del usuario.
     * @return DTO con los tokens de acceso y actualización.
     * @throws ResponseStatusException si el email o nombre público ya existen, o si el rol por defecto no se encuentra.
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
                .registrationDate(LocalDateTime.now()) // Fecha de registro actual.
                .build();

        // Asigna el rol por defecto "ROLE_USER".
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol por defecto no encontrado"));

        user.addRole(userRole);
        var savedUser = userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);

        return new TokenResponseDTO(jwtToken, refreshToken);
    }

    /**
     * Autentica a un usuario en el sistema.
     * @param request Datos de inicio de sesión del usuario.
     * @return DTO con los tokens de acceso y actualización.
     * @throws BadCredentialsException si las credenciales son incorrectas.
     * @throws UsernameNotFoundException si el usuario no existe.
     */
    @Transactional
    public TokenResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + request.email()));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return new TokenResponseDTO(jwtToken, refreshToken);
    }

    /**
     * Refresca el token de acceso de un usuario.
     * @param authHeader Encabezado de autorización con el token de actualización.
     * @return DTO con los nuevos tokens de acceso y actualización.
     * @throws ResponseStatusException si el token es inválido o no se puede refrescar.
     */
    @Transactional
    public TokenResponseDTO refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
        }

        final User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(userEmail));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de actualización inválido");
        }

        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return new TokenResponseDTO(accessToken, refreshToken);
    }

    /**
     * Guarda un nuevo token para un usuario.
     * @param user Usuario al que pertenece el token.
     * @param jwtToken Token JWT generado.
     */
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    /**
     * Revoca todos los tokens válidos de un usuario.
     * @param user Usuario cuyos tokens serán revocados.
     */
    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }
}