package org.project.group5.gamelend;

import org.project.group5.gamelend.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Clase principal para iniciar la aplicación GameLend
 * 
 * Esta aplicación gestiona el préstamo y compartición de videojuegos entre
 * usuarios,
 * permitiendo registrar juegos, solicitar y devolver préstamos, y gestionar
 * usuarios.
 * 
 * @author Grupo 5
 * @version 1.0
 */
@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class) // Habilita la configuración para almacenamiento de archivos
public class GameLendApp {

    /**
     * Método principal que inicia la aplicación Spring Boot
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SpringApplication.run(GameLendApp.class, args);
    }
}
