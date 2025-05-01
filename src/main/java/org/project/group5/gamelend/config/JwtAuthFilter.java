package org.project.group5.gamelend.config;

import java.io.IOException;
import java.util.Optional;

import org.project.group5.gamelend.entity.Token;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.repository.TokenRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.project.group5.gamelend.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para la autenticación mediante JWT.
 * Se ejecuta una vez por cada petición HTTP para verificar si el usuario está autenticado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_PATH = "/api/auth";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Bypass para rutas públicas de autenticación
        if (isAuthenticationPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener y validar header de autorización
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!isValidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Procesar token JWT
        String jwtToken = extractJwtToken(authHeader);
        processJwtToken(request, response, filterChain, jwtToken);
    }

    /**
     * Verifica si la ruta solicitada pertenece al path de autenticación
     */
    private boolean isAuthenticationPath(HttpServletRequest request) {
        return request.getServletPath().contains(AUTH_PATH);
    }

    /**
     * Verifica si el header de autorización es válido
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Extrae el token JWT del header de autorización
     */
    private String extractJwtToken(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Procesa el token JWT para autenticar al usuario
     */
    private void processJwtToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String jwtToken) throws ServletException, IOException {
        
        // Extraer nombre de usuario (email) del token
        String userEmail = jwtService.extractUsername(jwtToken);
        
        // Verificar si ya existe autenticación o el email es nulo
        if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Email nulo o usuario ya autenticado");
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar si el token existe y es válido en la base de datos
        if (!isTokenValidInDatabase(jwtToken)) {
            log.debug("Token no encontrado o inválido en la base de datos");
            filterChain.doFilter(request, response);
            return;
        }

        // Cargar detalles del usuario y verificar token
        authenticateUser(request, jwtToken, userEmail);
        
        // Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si el token es válido en la base de datos
     */
    private boolean isTokenValidInDatabase(String jwtToken) {
        Token token = tokenRepository.findByToken(jwtToken).orElse(null);
        return token != null && !token.isExpired() && !token.isRevoked();
    }

    /**
     * Autentica al usuario si el token es válido
     */
    private void authenticateUser(HttpServletRequest request, String jwtToken, String userEmail) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
        
        if (userOptional.isEmpty()) {
            log.debug("Usuario no encontrado en la base de datos: {}", userEmail);
            return;
        }
        
        User user = userOptional.get();
        if (!jwtService.isTokenValid(jwtToken, user)) {
            log.debug("Token JWT inválido para el usuario: {}", userEmail);
            return;
        }
        
        // Crear token de autenticación y establecer en el contexto
        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities());
        
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        log.debug("Usuario autenticado correctamente: {}", userEmail);
    }
}
