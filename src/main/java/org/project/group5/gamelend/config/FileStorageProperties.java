package org.project.group5.gamelend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración para el almacenamiento de archivos
 * 
 * Esta clase mapea las propiedades con prefijo "file" del archivo application.properties
 * a atributos Java para su uso en la aplicación. Principalmente controla el directorio
 * donde se almacenarán los archivos subidos por los usuarios.
 * 
 * Ejemplo de configuración en application.properties:
 * file.upload-dir=/ruta/almacenamiento/archivos
 */
@ConfigurationProperties(prefix = "file")
@Component
public class FileStorageProperties {
    
    /**
     * Directorio donde se guardarán los archivos subidos
     */
    private String uploadDir = "uploads"; // Valor por defecto

    /**
     * Constructor por defecto
     */
    public FileStorageProperties() {
    }

    /**
     * Constructor con parámetros
     * 
     * @param uploadDir Directorio para almacenar archivos
     */
    public FileStorageProperties(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * Obtiene el directorio de almacenamiento configurado
     * 
     * @return Ruta del directorio de almacenamiento
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Establece el directorio de almacenamiento
     * 
     * @param uploadDir Ruta del directorio donde se guardarán los archivos
     */
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}