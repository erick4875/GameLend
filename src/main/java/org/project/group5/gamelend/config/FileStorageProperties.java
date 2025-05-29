package org.project.group5.gamelend.config;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Configuración para el almacenamiento de archivos.
 * Lee propiedades con prefijo 'file' del application.properties.
 */
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    // Configuración de directorios
    private String uploadDir = "./uploads_gamelend";
    private String userImagesSubDir = "user_images";
    private String gameImagesSubDir = "game_images";
    private String defaultSubDir = "default_files";

    // Configuración de archivos
    private String maxSize = "10MB";
    private List<String> allowedExtensions = new ArrayList<>(
        List.of("png", "jpg", "jpeg", "gif")
    );

    /**
     * Obtiene la ruta completa para imágenes de usuario
     */
    public String getUserImagesPath() {
        return Paths.get(uploadDir, userImagesSubDir).toString();
    }

    /**
     * Obtiene la ruta completa para imágenes de juegos
     */
    public String getGameImagesPath() {
        return Paths.get(uploadDir, gameImagesSubDir).toString();
    }
    
    /**
     * Obtiene la ruta completa para archivos por defecto
     */
    public String getDefaultImagesPath() {
        return Paths.get(uploadDir, defaultSubDir).toString();
    }

    /**
     * Verifica si una extensión está permitida
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * Obtiene el tamaño máximo en bytes
     */
    public long getMaxSize() {
        return DataSize.parse(maxSize).toBytes();
    }

    // Getters y Setters estándar
    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    public String getUserImagesSubDir() { return userImagesSubDir; }
    public void setUserImagesSubDir(String dir) { this.userImagesSubDir = dir; }
    public String getGameImagesSubDir() { return gameImagesSubDir; }
    public void setGameImagesSubDir(String dir) { this.gameImagesSubDir = dir; }
    public String getDefaultSubDir() { return defaultSubDir; }
    public void setDefaultSubDir(String dir) { this.defaultSubDir = dir; }
    public void setMaxSize(String maxSize) { this.maxSize = maxSize; }
    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(List<String> extensions) { 
        this.allowedExtensions = extensions; 
    }
}