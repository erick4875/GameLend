package org.project.group5.gamelend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ConfigurationProperties(prefix = "file"): Carga propiedades desde `application.properties`
 *                                          que empiezan con "file." (ej. file.upload-dir).
 *
 * Almacena configuraciones para la subida de archivos.
 */
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    private String uploadDir; // Directorio principal para archivos subidos.
    private String gameImagesPath; // Subdirectorio para imágenes de juegos.
    private String userImagesPath; // Subdirectorio para imágenes de usuarios.
    private long maxSize; // Tamaño máximo de archivo permitido (en bytes).
    private List<String> allowedExtensions; // Extensiones de archivo permitidas (ej. "png", "jpg").

    // Getters y Setters: Necesarios para que Spring inyecte y otros servicios lean los valores.

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }

    public String getGameImagesPath() { return gameImagesPath; }
    public void setGameImagesPath(String gameImagesPath) { this.gameImagesPath = gameImagesPath; }

    public String getUserImagesPath() { return userImagesPath; }
    public void setUserImagesPath(String userImagesPath) { this.userImagesPath = userImagesPath; }

    public long getMaxSize() { return maxSize; }
    public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(List<String> allowedExtensions) { this.allowedExtensions = allowedExtensions; }

    /**
     * Comprueba si una extensión de archivo está en la lista de permitidas.
     */
    public boolean isExtensionAllowed(String extension) {
        // Si no hay lista de permitidas, se asume que todas lo son.
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        // Verifica si la extensión (ignorando mayúsculas/minúsculas) está en la lista.
        return allowedExtensions.stream().anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }
}