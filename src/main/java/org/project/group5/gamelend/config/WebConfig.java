package org.project.group5.gamelend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; 
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Configuration: Clase de configuración de Spring
 *
 * Configura CORS para permitir peticiones desde otros orígenes (frontend) al backend
 */
@Configuration
public class WebConfig {

    /**
     * @Bean: Define un WebMvcConfigurer para personalizar Spring MVC
     *         Usado aquí para la configuración global de CORS
     * 
     * @return Un WebMvcConfigurer con las reglas CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                // Configura CORS para rutas bajo "/api/**"
                registry.addMapping("/api/**")
                        // Orígenes permitidos para peticiones CORS
                        .allowedOrigins( 
                            "http://localhost",      // Navegador local
                            "http://localhost:8000", // Frontend web en desarroll
                            "app://localhost",       // Frameworks híbridos
                            "capacitor://localhost", // Capacitor
                            "http://10.0.2.2"        // Emulador Android (acceso al localhost del host)

                        ) 
                        // Métodos HTTP permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") 
                        // Cabeceras HTTP permitidas
                        .allowedHeaders("*") 
                        // Permite enviar credenciales (cookies, autenticación HTTP) en peticiones CORS
                        .allowCredentials(true) 
                        // Tiempo de caché para respuestas pre-vuelo OPTIONS (en segundos)
                        .maxAge(3600); 
            }
        };
    }
}
