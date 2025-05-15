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
 * @Component: Bean de Spring.
 * @RequiredArgsConstructor: Constructor con campos 'final'.
 * @Slf4j: Logger.
 *
 * Filtro que intercepta peticiones para validar tokens JWT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_PATH = "/api/auth"; // Rutas de autenticación que no necesitan token.

    // Inyección de dependencias.
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository; // Para validar tokens en BD.
    private final UserRepository userRepository;   // Para obtener el User completo.

    /**
     * Lógica principal del filtro para cada petición.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Si es ruta de autenticación, no procesar token.
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
     * Procesa el token: extrae email, valida y autentica.
     */
    private void processJwtToken(HttpServletRequest request, String jwtToken) {
        String userEmail = jwtService.extractUsername(jwtToken);
        
        // Si hay email y el usuario no está autenticado.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Si el token es válido en la BD (no expirado/revocado).
            if (isTokenValidInDatabase(jwtToken)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                authenticateUserIfTokenValid(request, jwtToken, userDetails);
            } else {
                log.debug("Token no válido en BD: {}", jwtToken);
            }
        }
    }

    /**
     * Verifica si el token existe en BD y no está expirado/revocado.
     */
    private boolean isTokenValidInDatabase(String jwtToken) {
        return tokenRepository.findByToken(jwtToken)
                .map(token -> !token.isExpired() && !token.isRevoked())
                .orElse(false);
    }

    /**
     * Si el token JWT es válido para UserDetails, autentica al usuario.
     */
    private void authenticateUserIfTokenValid(HttpServletRequest request, String jwtToken, UserDetails userDetails) {
        // Obtener User completo para validación con jwtService.
        Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
        
        if (userOptional.isEmpty()) {
            log.debug("Usuario no encontrado en BD para UserDetails: {}", userDetails.getUsername());
            return;
        }
        User user = userOptional.get();

        // Valida firma, expiración y si el token corresponde al usuario.
        if (jwtService.isTokenValid(jwtToken, user)) {
            // Crea token de autenticación para Spring Security.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Establece la autenticación en el contexto de Spring.
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Usuario autenticado: {}", userDetails.getUsername());
        } else {
            log.debug("Token JWT inválido para usuario: {}", userDetails.getUsername());
        }
    }
}
