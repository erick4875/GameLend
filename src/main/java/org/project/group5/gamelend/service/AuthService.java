package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.project.group5.gamelend.controller.RegisterRequest;
import org.project.group5.gamelend.controller.TokenResponse;
import org.project.group5.gamelend.dto.LoginRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        if (userRepository.existsByPublicName(request.publicName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre público ya está en uso");
        }

        var user = User.builder()
                .name(request.name())
                .publicName(request.publicName())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .province(request.province())
                .city(request.city())
                .registrationDate(LocalDateTime.now())
                .build();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol por defecto no encontrado"));

        user.addRole(userRole);
        var savedUser = userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);

        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse login(LoginRequest request) {
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

        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse refreshToken(final String authHeader) {
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

        return new TokenResponse(accessToken, refreshToken);
    }

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