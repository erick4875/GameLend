package org.project.group5.gamelend.config;

import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

/**
 * Configuración de autenticación y seguridad.
 * Gestiona la carga de usuarios y verificación de credenciales.
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    /** Repositorio para operaciones con usuarios */
    private final UserRepository userRepository;

    /**
     * Servicio para cargar usuarios por email.
     * Convierte nuestro User a UserDetails de Spring Security.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName()))
                            .toList())
                    .build();
        };
    }

    /**
     * Proveedor de autenticación que usa UserDetailsService y PasswordEncoder.
     * Maneja la lógica de verificación de credenciales.
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Gestor principal de autenticación.
     * Coordina el proceso de autenticación usando los providers configurados.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas usando BCrypt.
     * Proporciona hashing seguro para almacenar contraseñas.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}