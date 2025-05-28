package org.project.group5.gamelend.config; // Asegúrate que el paquete sea correcto

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties; // Importar Paths
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    private String uploadDir = "./uploads_gamelend"; // Directorio raíz por defecto
    private String userImagesSubDir = "users";    // Subdirectorio para imágenes de usuario
    private String gameImagesSubDir = "games";    // Subdirectorio para imágenes de juego
    private String defaultSubDir = "default_files"; // Subdirectorio por defecto

    private String maxSize = "10MB";
    private List<String> allowedExtensions = new ArrayList<>(List.of("png", "jpg", "jpeg", "gif"));

    // Getters y Setters para todos los campos que Spring necesita para el binding

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getUserImagesSubDir() {
        return userImagesSubDir;
    }

    public void setUserImagesSubDir(String userImagesSubDir) {
        this.userImagesSubDir = userImagesSubDir;
    }

    public String getGameImagesSubDir() {
        return gameImagesSubDir;
    }

    public void setGameImagesSubDir(String gameImagesSubDir) {
        this.gameImagesSubDir = gameImagesSubDir;
    }

    public String getDefaultSubDir() {
        return defaultSubDir;
    }

    public void setDefaultSubDir(String defaultSubDir) {
        this.defaultSubDir = defaultSubDir;
    }

    // Métodos de conveniencia para obtener las rutas completas
    public String getUserImagesPath() {
        return Paths.get(uploadDir, userImagesSubDir).toString();
    }

    public String getGameImagesPath() {
        return Paths.get(uploadDir, gameImagesSubDir).toString();
    }
    
    public String getDefaultImagesPath() { // Para el caso 'default' en FileStorageService
        return Paths.get(uploadDir, defaultSubDir).toString();
    }

    public long getMaxSize() {
        return DataSize.parse(maxSize).toBytes();
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return allowedExtensions.contains(extension.toLowerCase());
    }
}