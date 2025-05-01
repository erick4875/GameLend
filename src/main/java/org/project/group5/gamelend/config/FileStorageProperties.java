package org.project.group5.gamelend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Configuración para el almacenamiento de imágenes.
 * Mapea las propiedades con prefijo "file" del archivo application.properties.
 * 
 * Ejemplo de configuración en application.properties:
 * file.upload-dir=uploads/images
 * file.max-size=5242880
 * file.allowed-extensions=jpg,jpeg
 */
@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FileStorageProperties {

    /**
     * Directorio donde se guardarán las imágenes subidas
     */
    @NotBlank(message = "El directorio de carga no puede estar en blanco")
    private String uploadDir = "uploads/images";
    
    /**
     * Tamaño máximo permitido para imágenes (en bytes)
     * Por defecto: 5MB (suficiente para imágenes de alta calidad)
     */
    @Positive(message = "El tamaño máximo debe ser un valor positivo")
    private long maxSize = 5 * 1024 * 1024;
    
    /**
     * Extensiones de imagen permitidas: JPG y JPEG
     */
    @NotBlank(message = "Las extensiones permitidas no pueden estar vacías")
    private String allowedExtensions = "jpg,jpeg";
    
    /**
     * Determina si se deben crear subdirectorios por tipo (games/users)
     */
    private boolean createTypeSubdirs = true;
    
    /**
     * Verifica si una extensión está permitida
     * 
     * @param extension La extensión a verificar (sin el punto)
     * @return true si la extensión está permitida, false en caso contrario
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        
        String ext = extension.toLowerCase().trim();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        
        for (String allowedExt : allowedExtensions.split(",")) {
            if (allowedExt.trim().equals(ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Obtiene la ruta para guardar imágenes de juegos
     */
    public String getGameImagesPath() {
        return createTypeSubdirs ? uploadDir + "/games" : uploadDir;
    }
    
    /**
     * Obtiene la ruta para guardar imágenes de usuarios
     */
    public String getUserImagesPath() {
        return createTypeSubdirs ? uploadDir + "/users" : uploadDir;
    }
}