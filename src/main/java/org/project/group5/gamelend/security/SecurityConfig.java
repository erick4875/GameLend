package org.project.group5.gamelend.security;

import org.project.group5.gamelend.config.JwtAuthFilter;
import org.project.group5.gamelend.repository.TokenRepository; // Asegúrate que esta importación sea necesaria aquí
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider; // Asegúrate de importar HttpMethod
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
@EnableMethodSecurity // Habilita @PreAuthorize, etc.
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final TokenRepository tokenRepository; // Usado en el logout handler

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(req -> req
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Endpoints públicos
                .requestMatchers("/api/documents/**").permitAll() 
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN") 
                .requestMatchers("/api/games/**").hasAnyRole("USER", "ADMIN") // Ejemplo si los juegos también necesitan autenticación
                .anyRequest().authenticated() // Todas las demás rutas requieren autenticación
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return; 
                    }
                    final String jwtToken = authHeader.substring(7);
                    tokenRepository.findByToken(jwtToken).ifPresent(token -> {
                        token.setExpired(true);
                        token.setRevoked(true);
                        tokenRepository.save(token);
                    });
                })
                .logoutSuccessHandler((request, response, authentication) ->
                    SecurityContextHolder.clearContext()
                )
            );

        return http.build();
    }
}