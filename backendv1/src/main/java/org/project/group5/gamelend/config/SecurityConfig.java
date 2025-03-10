package org.project.group5.gamelend.config;

import java.util.Arrays;

import org.project.group5.gamelend.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración centralizada de seguridad para la aplicación
 * 
 * Gestiona la autenticación, autorización, JWT, CORS y otras configuraciones
 * de seguridad para proteger los endpoints de la API.
 */
@Configuration
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        logger.info("Inicializando configuración de seguridad");
    }

    /**
     * Configura el gestor de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("Configurando AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configura la cadena de filtros de seguridad
     * Define reglas de acceso, manejo de sesiones, CSRF, CORS y filtros JWT
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configurando SecurityFilterChain");
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/juegos").permitAll()
                .requestMatchers("/api/documento/download/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Añadir endpoints de diagnóstico
                .requestMatchers("/api/diagnostic/**").permitAll() // TEMPORAL para diagnóstico
                .requestMatchers(HttpMethod.GET, "/api/usuarios").permitAll() // TEMPORAL para diagnóstico
                // Rutas protegidas por rol
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated() // Cualquier otro endpoint requiere autenticación pero no roles específicos
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        logger.info("Configuración de seguridad completada");
        return http.build();
    }

    /**
     * Configura el filtro de autenticación JWT
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        logger.debug("Creando filtro de autenticación JWT");
        return new JwtAuthenticationFilter(secretKey, customUserDetailsService);
    }

    /**
     * Configura el codificador de contraseñas (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Configurando codificador de contraseñas BCrypt");
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura CORS necesaria para permitir peticiones desde aplicaciones web
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("Configurando políticas CORS");
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        // O bien, si necesitas aceptar cualquier origen:
        // configuration.addAllowedOrigin("*");
        // configuration.setAllowCredentials(false); // No usar credenciales con wildcard

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Disposition"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
