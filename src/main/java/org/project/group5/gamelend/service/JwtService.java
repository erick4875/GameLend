package org.project.group5.gamelend.service;

import java.util.Date;
import java.util.Map;

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
    private Long jwtExpiration; // Tiempo de expiración del token en milisegundos

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration; // Tiempo de expiración del token de refresco en milisegundos

    /**
     * Genera un token JWT de acceso para el usuario.
     */
    public String generateToken(final User user) {
        return buildToken(user, jwtExpiration);
    }

    /**
     * Genera un token JWT de refresco para el usuario.
     */
    public String generateRefreshToken(final User user) {
        return buildToken(user, refreshExpiration);
    }

    /**
     * Construye un token JWT con el usuario y el tiempo de expiración indicado.
     */
    private String buildToken(final User user, final long expiration) {
        return Jwts.builder()
                .id(user.getId().toString())
                .claims(Map.of("name", user.getName()))
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Obtiene la clave secreta para firmar el token JWT.
     */
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario (email) del token JWT.
     */
    public String extractUsername(final String token) {
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return jwtToken.getSubject();
    }

    /**
     * Extrae el ID del usuario del token JWT.
     */
    public Long extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("id", Long.class);
    }

    /**
     * Extrae un claim específico del token usando la función proporcionada.
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
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
     * Verifica si el token es válido comparando el nombre de usuario y la fecha de expiración.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String publicName = extractUsername(token);
        return (publicName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     */
    private Date extractExpiration(final String token) {
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return jwtToken.getExpiration();
    }

    /**
     * Verifica si el token es válido comparando el email y la fecha de expiración.
     */
    public boolean isTokenValid(final String token, final User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /**
     * Verifica si el token ha expirado.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}