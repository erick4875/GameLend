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

// implementa la lógica de negocio relacionada con la generación y validación de tokens JWT
// y la gestión de la autenticación de usuarios. Utiliza la biblioteca jjwt para crear y verificar tokens JWT.
@Service
@Slf4j
public class JwtService {
    // dentro de value se usa el nombre de la propiedad definida en el archivo
    // application.properties
    @Value("${jwt.secret-key}")
    private String secretKey; // Clave secreta para firmar el token JWT

    @Value("${jwt.expiration}")
    private Long jwtExpiration; // Tiempo de expiración del token en milisegundos

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration; // Tiempo de expiración del token de refresco en milisegundos

    // El token de acceso tiene un tiempo de expiración de 1 día (86400000 ms)
    public String generateToken(final User user) {
        return buildToken(user, jwtExpiration);
    }

    // El token de refresco tiene un tiempo de expiración de 7 días (604800000 ms)
    public String generateRefreshToken(final User user) {
        return buildToken(user, refreshExpiration);
    }

    // método que recibe un objeto User y un tiempo de expiración en milisegundos
    // pasamos los parametros al método buildToken para crear el token JWT
    private String buildToken(final User user, final long expiration) {
        return Jwts.builder()
                .id(user.getId().toString()) // Establece el ID del usuario en el token
                .claims(Map.of("name", user.getName())) // información adicional del usuario que queremos dar a nuestra
                                                        // token
                .subject(user.getEmail()) // como sera identificado el usuario en el token (email)
                .issuedAt(new Date(System.currentTimeMillis())) // Establece fecha creación del token
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Establece la fecha de expiración del
                                                                               // token
                .signWith(getSignInKey()) // registrado el token con la clave secreta
                .compact(); // Genera el token JWT
    }

    // Obtiene la clave secreta para firmar el token JWT
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Decodifica la clave secreta de application.properties
        return Keys.hmacShaKeyFor(keyBytes); // Crea una clave secreta HMAC a partir de los bytes decodificados con el
                                             // algoritmo SHA-256
    }

    // Extrae el nombre público del usuario del token JWT
    public String extractUsername(final String token) {
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // información del token JWT
        return jwtToken.getSubject();
    }

    /**
     * Extrae el ID del usuario del token JWT
     */
    public Long extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("id", Long.class);
    }

    /**
     * Extrae un claim específico del token usando la función proporcionada
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Verifica si el token es válido comparando el nombre público y la fecha de
    // expiración
    public boolean validateToken(String token, UserDetails userDetails) {
        final String publicName = extractUsername(token);
        return (publicName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Date extractExpiration(final String token) {
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return jwtToken.getExpiration();
    }

    // Verifica si el token es válido comparando el nombre público y la fecha de
    // expiración
    public boolean isTokenValid(final String token, final User user) {
        // Extrae el nombre de usuario del token
        final String username = extractUsername(token);
        // Compara el nombre de usuario extraído del token con el nombre de usuario
        // (email) del objeto User y verifica si el token no ha expirado
        return (username.equals(user.getEmail()) && !isTokenExpired(token));
    }

    // Verifica si el token ha expirado
    private boolean isTokenExpired(String token) {
        // Compara la fecha de expiración del token con la fecha actual
        // Si la fecha de expiración es anterior a la fecha actual, el token ha expirado
        // y se devuelve true, indicando que el token no es válido para su uso.
        // Si la fecha de expiración es posterior a la fecha actual, el token es válido
        // y se devuelve false, indicando que el token no ha expirado
        return extractExpiration(token).before(new Date());
    }

}