package org.project.group5.gamelend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS para permitir acceso desde aplicaciones frontend.
 * Habilita la comunicación entre el backend y diferentes clientes (web, móvil,
 * etc).
 */
@Configuration
public class WebConfig {

    /**
     * Configura las reglas CORS globales para la aplicación.
     */
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Orígenes permitidos
                        .allowedOrigins(
                                "http://localhost", // Web local
                                "http://localhost:8000", // Web desarrollo
                                "app://localhost", // Apps híbridas
                                "capacitor://localhost", // Apps Capacitor
                                "http://10.0.2.2" // Emulador Android
                )
                        // Métodos HTTP permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        // Headers permitidos
                        .allowedHeaders("*")
                        // Permite credenciales
                        .allowCredentials(true)
                        // Caché de pre-vuelo (1 hora)
                        .maxAge(3600);
            }
        };
    }
}
