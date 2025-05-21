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
 * @Configuration: Clase de configuración de Spring.
 * @RequiredArgsConstructor: Lombok crea un constructor para campos 'final' (inyección de dependencias).
 *
 * Configura cómo Spring Security carga usuarios, verifica contraseñas y gestiona la autenticación.
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    // Repositorio para acceder a los datos de los usuarios.
    private final UserRepository userRepository;

    /**
     * @Bean: Define un 'UserDetailsService'.
     * UserDetailsService: Carga datos del usuario (por email en este caso) para Spring Security.
     * 
     * @return Implementación que busca usuarios en la BD por email.
     */
    @Bean
    UserDetailsService userDetailsService() {
        // Busca un usuario por email. Si no lo encuentra, lanza una excepción.
        return email -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario con email '%s' no encontrado".formatted(email)));
            
            // Crea un objeto UserDetails de Spring Security con el email, contraseña (hasheada) y roles del usuario.
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
     * @Bean: Define un 'AuthenticationProvider'.
     * AuthenticationProvider: Se encarga de la lógica de autenticación.
     *                         DaoAuthenticationProvider usa UserDetailsService y PasswordEncoder.
     * 
     * @return Un DaoAuthenticationProvider configurado.
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Le dice cómo cargar los usuarios.
        provider.setUserDetailsService(userDetailsService());
        // Le dice cómo verificar las contraseñas.
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * @Bean: Define el 'AuthenticationManager'.
     * AuthenticationManager: Gestiona el proceso de autenticación, usando los AuthenticationProviders.
     * 
     * @param config Configuración de autenticación de Spring.
     * @return El AuthenticationManager.
     * @throws Exception Si hay error al obtener el AuthenticationManager.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * @Bean: Define un 'PasswordEncoder'.
     * PasswordEncoder: Se usa para codificar (hashear) contraseñas de forma segura.
     * 
     * @return Un BCryptPasswordEncoder, que es un algoritmo de hashing fuerte.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}