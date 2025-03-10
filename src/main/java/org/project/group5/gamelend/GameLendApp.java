package org.project.group5.gamelend;

import org.project.group5.gamelend.config.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Clase principal para iniciar la aplicación GameLend
 * 
 * Esta aplicación gestiona el préstamo y compartición de videojuegos entre usuarios,
 * permitiendo registrar juegos, solicitar y devolver préstamos, y gestionar usuarios.
 * 
 * @author Grupo 5
 * @version 1.0
 */
@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class) // Habilita la configuración para almacenamiento de archivos
public class GameLendApp {
    
    private static final Logger logger = LoggerFactory.getLogger(GameLendApp.class);
    
    /**
     * Método principal que inicia la aplicación Spring Boot
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        logger.info("Iniciando aplicación GameLend...");
        SpringApplication.run(GameLendApp.class, args);
        logger.info("Aplicación GameLend iniciada correctamente");
    }
}
