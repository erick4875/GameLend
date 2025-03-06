package org.project.group5.gamelend.controller;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.service.UsuarioService;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Controlador para gestionar operaciones de autenticación
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    @Value("${jwt.secret}")
    private String secretKey;

    public AuthController(PasswordEncoder passwordEncoder, UsuarioService usuarioService) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    /**
     * Procesa solicitudes de login
     * 
     * @param request Credenciales de usuario
     * @return Respuesta con token JWT si la autenticación es exitosa
     */
    @PostMapping("/login")
    public ResponseEntity<RespuestaGeneral<AuthResponse>> login(@RequestBody LoginRequest request) {
        logger.info("Intento de login para usuario: {}", request.getEmail());

        // Validación de entrada
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            logger.warn("Intento de login con credenciales nulas o vacías");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                            RespuestaGlobal.RESP_ERROR,
                            "Email y contraseña son requeridos",
                            null));
        }

        try {
            // Usar el servicio para obtener el usuario
            RespuestaGeneral<Usuario> respuesta = usuarioService.getUsuarioByEmail(request.getEmail());

            if (respuesta.isExito() && respuesta.getCuerpo() != null) {
                Usuario usuario = respuesta.getCuerpo();

                // Verificar contraseña
                if (passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                    logger.info("Login exitoso para usuario: {}", request.getEmail());

                    // Generar JWT de manera más segura
                    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

                    String token = Jwts.builder()
                            .setSubject(usuario.getEmail())
                            .claim("id", usuario.getId())
                            .claim("nombrePublico", usuario.getNombrePublico())
                            .setIssuedAt(new Date())
                            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();

                    AuthResponse authResponse = new AuthResponse(token, usuario.getNombrePublico(), usuario.getId());
                    return ResponseEntity.ok(
                            new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                                    RespuestaGlobal.RESP_OK,
                                    RespuestaGlobal.OPER_CORRECTA,
                                    authResponse));
                }
            }

            logger.warn("Credenciales incorrectas para: {}", request.getEmail());
            return ResponseEntity.status(401).body(
                    new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                            RespuestaGlobal.RESP_ERROR,
                            "Credenciales incorrectas",
                            null));

        } catch (Exception e) {
            logger.error("Error durante la autenticación: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                            RespuestaGlobal.RESP_ERROR,
                            "Error en el servidor: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Registra un nuevo usuario
     * 
     * @param usuario Datos del nuevo usuario
     * @return Respuesta indicando el resultado de la operación
     */
    @PostMapping("/register")
    public ResponseEntity<RespuestaGeneral<AuthResponse>> register(@RequestBody Usuario usuario) {
        logger.info("Intento de registro para email: {}", usuario.getEmail());

        // Validación básica
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
                usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            logger.warn("Datos de registro incompletos");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                            RespuestaGlobal.RESP_ERROR,
                            "Email y contraseña son requeridos",
                            null));
        }

        try {
            // Verificar si el usuario ya existe
            if (usuarioService.getUsuarioByEmail(usuario.getEmail()).isExito()) {
                logger.warn("Intento de registro con email existente: {}", usuario.getEmail());
                return ResponseEntity.badRequest().body(
                        new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                                RespuestaGlobal.RESP_ERROR,
                                "El email ya está registrado",
                                null));
            }

            // Encriptar contraseña
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            // formato de fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            usuario.setFechaRegistro(LocalDateTime.now());

            // Guardar usuario
            RespuestaGeneral<Usuario> respuesta = usuarioService.saveUsuario(usuario);

            if (respuesta.isExito() && respuesta.getCuerpo() != null) {
                Usuario nuevoUsuario = respuesta.getCuerpo();
                logger.info("Usuario registrado correctamente: {}", usuario.getEmail());

                // Generar token para inicio de sesión automático
                SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

                String token = Jwts.builder()
                        .setSubject(nuevoUsuario.getEmail())
                        .claim("id", nuevoUsuario.getId())
                        .claim("nombrePublico", nuevoUsuario.getNombrePublico())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                        .signWith(key, SignatureAlgorithm.HS256)
                        .compact();

                AuthResponse authResponse = new AuthResponse(token, nuevoUsuario.getNombrePublico(),
                        nuevoUsuario.getId());
                return ResponseEntity.ok(
                        new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                                RespuestaGlobal.RESP_OK,
                                "Usuario registrado correctamente",
                                authResponse));
            } else {
                logger.error("Error al registrar usuario: {}", respuesta.getMensaje());
                return ResponseEntity.status(500).body(
                        new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                                RespuestaGlobal.RESP_ERROR,
                                "Error al registrar usuario: " + respuesta.getMensaje(),
                                null));
            }

        } catch (Exception e) {
            logger.error("Error durante el registro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    new RespuestaGeneral<>(RespuestaGlobal.TIPO_AUTH,
                            RespuestaGlobal.RESP_ERROR,
                            "Error en el servidor: " + e.getMessage(),
                            null));
        }
    }
}

/**
 * DTO para recibir los datos de login
 */
class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

/**
 * DTO para devolver el token y datos básicos del usuario
 */
class AuthResponse {
    private String token;
    private String nombrePublico;
    private Long userId;

    public AuthResponse(String token, String nombrePublico, Long userId) {
        this.token = token;
        this.nombrePublico = nombrePublico;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public String getNombrePublico() {
        return nombrePublico;
    }

    public Long getUserId() {
        return userId;
    }
}
