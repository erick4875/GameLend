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
 * Configuración de autenticación y seguridad para la aplicación
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;

    /**
     * Define el servicio de detalles de usuario utilizado por Spring Security
     * para cargar información de usuarios durante la autenticación.
     * 
     * @return Implementación personalizada de UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("Usuario con email '%s' no encontrado", email)));
            
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
     * Define el proveedor de autenticación que utilizará el UserDetailsService
     * y el PasswordEncoder configurados.
     * 
     * @return Proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Define el administrador de autenticación utilizando la configuración predeterminada.
     * 
     * @param config Configuración de autenticación proporcionada por Spring
     * @return Gestor de autenticación configurado
     * @throws Exception si hay problemas al obtener el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define el codificador de contraseñas utilizando BCrypt.
     * 
     * @return Instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}