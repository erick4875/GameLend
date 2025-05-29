package org.project.group5.gamelend.config;

import java.io.IOException;
import java.util.Optional;

import org.project.group5.gamelend.entity.User; // Asegúrate de que esta entidad Token esté definida si la usas en TokenRepository
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
 * Filtro de autenticación JWT.
 * Valida tokens JWT en las peticiones HTTP.
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

    /**
     * Lógica principal del filtro para cada petición.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Saltar validación para rutas de autenticación
        if (isAuthenticationPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener cabecera 'Authorization'.
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Si no es válida (null o sin "Bearer "), no procesar token.
        if (!isValidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer y procesar el token JWT.
        String jwtToken = extractJwtToken(authHeader);
        processJwtToken(request, jwtToken);
        
        filterChain.doFilter(request, response); // Continuar con el siguiente filtro.
    }

    /**
     * Verifica si la ruta es de autenticación.
     */
    private boolean isAuthenticationPath(HttpServletRequest request) {
        return request.getServletPath().contains(AUTH_PATH);
    }

    /**
     * Verifica si la cabecera de autorización es válida.
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Extrae el token JWT (sin "Bearer ").
     */
    private String extractJwtToken(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Procesa y valida el token JWT
     */
    private void processJwtToken(HttpServletRequest request, String jwtToken) {
        String userEmail = jwtService.extractUsername(jwtToken);
        
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (isTokenValidInDatabase(jwtToken)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                authenticateUserIfTokenValid(request, jwtToken, userDetails);
            }
        }
    }

    /**
     * Verifica validez del token en base de datos
     */
    private boolean isTokenValidInDatabase(String jwtToken) {
        return tokenRepository.findByToken(jwtToken)
                .map(token -> !token.isExpired() && !token.isRevoked())
                .orElse(false);
    }

    /**
     * Autentica al usuario si el token es válido
     */
    private void authenticateUserIfTokenValid(HttpServletRequest request, String jwtToken, UserDetails userDetails) {
        Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
        
        if (userOptional.isEmpty()) {
            log.debug("Usuario no encontrado: {}", userDetails.getUsername());
            return;
        }

        User user = userOptional.get();
        if (jwtService.isTokenValid(jwtToken, user)) {
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Usuario autenticado: {}", userDetails.getUsername());
        }
    }
}
