package org.project.group5.gamelend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.project.group5.gamelend.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro para la autenticación basada en JWT
 * 
 * Este filtro intercepta todas las solicitudes HTTP y verifica si contienen un token JWT válido.
 * Si el token es válido, establece el contexto de seguridad para permitir el acceso a recursos protegidos.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final String secretKey;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor que recibe la clave secreta para validación de tokens
     * 
     * @param secretKey Clave secreta configurada en la aplicación
     */
    public JwtAuthenticationFilter(String secretKey, CustomUserDetailsService userDetailsService) {
        this.secretKey = secretKey;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Procesa cada solicitud HTTP para verificar y validar el token JWT
     * 
     * @param request Solicitud HTTP entrante
     * @param response Respuesta HTTP
     * @param filterChain Cadena de filtros para continuar el procesamiento
     * @throws ServletException Si ocurre un error en el servlet
     * @throws IOException Si ocurre un error de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Obtener el token JWT del encabezado Authorization
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                // Validar y procesar el token
                processToken(token, request);
            }
            
            // Continuar con el siguiente filtro en la cadena
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token malformado");
        } catch (SignatureException e) {
            logger.error("Error de firma en token JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Firma del token inválida");
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Formato de token no soportado");
        } catch (Exception e) {
            logger.error("Error al procesar token JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación");
        }
    }
    
    /**
     * Extrae el token JWT de la solicitud HTTP
     * 
     * @param request Solicitud HTTP
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * Procesa y valida el token JWT
     * 
     * @param token Token JWT a validar
     * @param request Solicitud HTTP actual
     */
    private void processToken(String token, HttpServletRequest request) {
        // Usar la clave segura para verificar el token
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userEmail = claims.getSubject();
        
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar el usuario con sus roles desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            // Crear autenticación en el contexto de seguridad
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Usuario autenticado: {} con roles: {}", userEmail, userDetails.getAuthorities());
        }
    }
    
    /**
     * Envía respuesta de error al cliente
     * 
     * @param response Respuesta HTTP
     * @param statusCode Código de estado HTTP
     * @param message Mensaje de error
     * @throws IOException Si ocurre un error al escribir la respuesta
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}


