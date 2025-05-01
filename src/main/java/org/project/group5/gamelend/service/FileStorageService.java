package org.project.group5.gamelend.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.project.group5.gamelend.config.FileStorageProperties;
import org.project.group5.gamelend.exception.FileStorageException;
import org.project.group5.gamelend.exception.MyFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar el almacenamiento de imágenes para juegos y usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageProperties storageProperties;
    
    /**
     * Tipo de imagen que determina la ubicación de almacenamiento
     */
    public enum ImageType {
        GAME,
        USER
    }

    /**
     * Almacena una imagen en el sistema de archivos
     * 
     * @param file Archivo de imagen recibido
     * @param type Tipo de imagen (GAME o USER)
     * @return Nombre del archivo almacenado
     * @throws FileStorageException Si hay problemas al almacenar el archivo
     */
    public String storeImage(MultipartFile file, ImageType type) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("No se puede almacenar un archivo vacío");
        }
        
        // Verificar tamaño
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new FileStorageException("El tamaño del archivo excede el límite permitido de " 
                    + (storageProperties.getMaxSize() / 1024 / 1024) + "MB");
        }
        
        // Obtener y verificar extensión de forma segura
        String originalFilename = file.getOriginalFilename();
        // Usar nombre predeterminado si es nulo
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "imagen_sin_nombre.jpg";
        } else {
            originalFilename = StringUtils.cleanPath(originalFilename);
        }
        
        String extension = getFileExtension(originalFilename);
        
        // Verificar que la extensión no esté vacía
        if (extension.isEmpty()) {
            extension = "jpg"; // Extensión por defecto
        }
        
        if (!storageProperties.isExtensionAllowed(extension)) {
            throw new FileStorageException("Solo se permiten archivos con extensiones: " 
                    + storageProperties.getAllowedExtensions());
        }
        
        // Resto del código sin cambios
        String newFilename = generateUniqueFilename(extension);
        Path targetLocation = getTargetLocation(type, newFilename);
        
        try {
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation);
            log.info("Imagen almacenada exitosamente: {}", newFilename);
            
            return newFilename;
        } catch (IOException e) {
            throw new FileStorageException("Error al almacenar la imagen " + newFilename, e);
        }
    }
    
    /**
     * Almacena una imagen a partir de un array de bytes
     * 
     * @param imageBytes Contenido de la imagen en bytes
     * @param type Tipo de imagen (GAME o USER)
     * @param extension Extensión del archivo (sin punto)
     * @return Nombre del archivo almacenado
     * @throws FileStorageException Si hay problemas al almacenar el archivo
     */
    public String storeImage(byte[] imageBytes, ImageType type, String extension) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new FileStorageException("No se puede almacenar una imagen vacía");
        }
        
        // Verificar tamaño
        if (imageBytes.length > storageProperties.getMaxSize()) {
            throw new FileStorageException("El tamaño de la imagen excede el límite permitido de " 
                    + (storageProperties.getMaxSize() / 1024 / 1024) + "MB");
        }
        
        // Validar y limpiar extensión
        if (extension == null || extension.isEmpty()) {
            extension = "jpg"; // Extensión por defecto
        } else if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        
        extension = extension.toLowerCase();
        
        if (!storageProperties.isExtensionAllowed(extension)) {
            throw new FileStorageException("Solo se permiten archivos con extensiones: " 
                    + storageProperties.getAllowedExtensions());
        }
        
        // Generar nombre único para el archivo
        String filename = generateUniqueFilename(extension);
        
        // Determinar ubicación según tipo
        Path targetLocation = getTargetLocation(type, filename);
        
        try {
            // Asegurar que existe el directorio
            Files.createDirectories(targetLocation.getParent());
            
            // Escribir archivo
            Files.write(targetLocation, imageBytes);
            log.info("Imagen almacenada exitosamente: {}", filename);
            
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Error al almacenar la imagen " + filename, e);
        }
    }
    
    /**
     * Carga una imagen como recurso
     * 
     * @param filename Nombre del archivo
     * @param type Tipo de imagen (GAME o USER)
     * @return Recurso que representa la imagen
     * @throws MyFileNotFoundException Si la imagen no existe
     */
    public Resource loadImageAsResource(String filename, ImageType type) {
        try {
            Path filePath = getTargetLocation(type, filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("Imagen no encontrada: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new MyFileNotFoundException("Error al acceder a la imagen: " + filename, e);
        }
    }
    
    /**
     * Elimina una imagen
     * 
     * @param filename Nombre del archivo
     * @param type Tipo de imagen (GAME o USER)
     * @return true si la imagen fue eliminada, false en caso contrario
     */
    public boolean deleteImage(String filename, ImageType type) {
        try {
            Path filePath = getTargetLocation(type, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error al eliminar la imagen {}: {}", filename, e.getMessage());
            return false;
        }
    }
    
    /**
     * Genera un nombre de archivo único basado en timestamp y UUID
     * 
     * @param extension Extensión del archivo (sin punto)
     * @return Nombre de archivo único
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }
    
    /**
     * Obtiene la extensión de un nombre de archivo
     * 
     * @param filename Nombre completo del archivo
     * @return Extensión sin punto o cadena vacía si no tiene extensión
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Determina la ubicación para almacenar un archivo según su tipo
     * 
     * @param type Tipo de imagen
     * @param filename Nombre del archivo
     * @return Path completo donde almacenar el archivo
     */
    private Path getTargetLocation(ImageType type, String filename) {
        Path basePath = switch (type) {
            case GAME -> Paths.get(storageProperties.getGameImagesPath()).toAbsolutePath().normalize();
            case USER -> Paths.get(storageProperties.getUserImagesPath()).toAbsolutePath().normalize();
            default -> Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        };
        
        return basePath.resolve(filename);
    }
}
