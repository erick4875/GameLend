package org.project.group5.gamelend.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.project.group5.gamelend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la generación y validación de tokens JWT.
 * Utiliza la biblioteca jjwt para crear y verificar tokens JWT.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey; // Clave secreta para firmar el token JWT

    @Value("${jwt.expiration}")
    private Long jwtExpiration; // Tiempo de expiración para el access token

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration; // Tiempo de expiración del token de refresco

    public static final String TOKEN_TYPE_CLAIM = "type"; // Tipo de claim para el token
    public static final String ACCESS_TOKEN_TYPE = "access"; // Tipo de token de acceso
    public static final String REFRESH_TOKEN_TYPE = "refresh"; // Tipo de token de refresco

    /**
     * Genera un token JWT de acceso para el usuario.
     */
    public String generateToken(final User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE); // Añade el tipo de token al claim
        extraClaims.put("publicName", user.getPublicName()); // Añade el nombre del usuario al claim
        return buildToken(extraClaims, user, jwtExpiration);
    }

    /**
     * Construye un token JWT con los claims extra, el usuario y el tiempo de
     * expiración indicado.
     */
    private String buildToken(Map<String, Object> extraClaims, final User user, final long expiration) {
        return Jwts.builder()
                .claims(extraClaims) // Usar .claims() para añadir un mapa de claims
                .id(user.getId().toString()) // añadir el ID del usuario para identificación
                .subject(user.getEmail()) // 'sub' (Subject), el identificador principal del usuario
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Genera un token JWT de refresco para el usuario.
     */
    public String generateRefreshToken(final User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        return buildToken(extraClaims, user, refreshExpiration);
    }

    /**
     * Obtiene la clave secreta para firmar el token JWT.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario (email) del token JWT (del claim 'subject').
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token JWT (del claim 'jti' o 'id').
     */
    public Long extractUserId(String token) {
        String idStr = extractClaim(token, Claims::getId);
        return (idStr != null) ? Long.parseLong(idStr) : null;
    }

    /**
     * Extrae el tipo de token ("access" o "refresh") del claim "typ".
     */
    public String extractTokenType(String token) {
        try {
            return extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            log.warn("No se pudo extraer el tipo de token (claim '{}') del token: {}", TOKEN_TYPE_CLAIM,
                    e.getMessage());
            return null; // o lanzar una excepción específica si el claim es obligatorio
        }
    }

    /**
     * Extrae un claim específico del token usando la función proporcionada.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si el token de acceso es válido (usuario, expiración y tipo).
     * Este método ahora es específico para access tokens.
     */
    public boolean isAccessTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tokenType = extractTokenType(token);
        return (ACCESS_TOKEN_TYPE.equals(tokenType) &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));
    }

    /**
     * Verifica si el token de refresco es válido (usuario, expiración y tipo).
     */
    public boolean isRefreshTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tokenType = extractTokenType(token);
        return (REFRESH_TOKEN_TYPE.equals(tokenType) &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));
    }

    /**
     * Verifica si el token (cualquier tipo) es válido para el usuario y no ha
     * expirado.
     * Deberías decidir si lo mantienes o usas los más específicos.
     */
    public boolean isTokenValid(final String token, final User user) { 
        final String username = extractUsername(token);
        return (username.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /**
     * Verifica si el token (cualquier tipo) es válido para el UserDetails y no ha
     * expirado.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     */
    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si el token ha expirado.
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            // token experiado o no válido
            log.warn("Error al verificar la expiración del token: {}", e.getMessage());
            return true;
        }
    }
}