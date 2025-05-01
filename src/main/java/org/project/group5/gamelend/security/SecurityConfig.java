package org.project.group5.gamelend.security;

import org.project.group5.gamelend.config.JwtAuthFilter;
import org.project.group5.gamelend.entity.Token;
import org.project.group5.gamelend.repository.TokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final TokenRepository tokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitar CSRF (Cross-Site Request Forgery)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Solo admin
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated() // Todas las demás rutas requieren autenticación
                )
                // Sin estado (stateless) - no se guarda el estado de la sesión del usuario
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // verificar si el usuario está autenticado
                .authenticationProvider(authenticationProvider)
                // trigger ; antes de confirmar si el usuario está autenticado, se ejecuta el
                // filtro de autenticación
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // URL de cierre de sesión
                        .addLogoutHandler((request, response, authentication) -> {
                            final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                            logout(authHeader);
                        })
                        .logoutSuccessHandler((request, response, authentication) ->
                        // Limpiar el contexto de seguridad al desloguear
                        SecurityContextHolder.clearContext()));

        return http.build();
    }

    private void logout(final String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid token");
        }

        final String jwtToken = token.substring(7);
        final Token foundToken = tokenRepository.findByToken(jwtToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        foundToken.setExpired(true);
        foundToken.setRevoked(true);
        tokenRepository.save(foundToken);
    }

}