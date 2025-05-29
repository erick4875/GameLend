package org.project.group5.gamelend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; // Import Paths
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.project.group5.gamelend.config.FileStorageProperties;
import org.project.group5.gamelend.exception.FileStorageException;
import org.project.group5.gamelend.exception.MyFileNotFoundException; // Assuming you have this custom exception
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct; // Import PostConstruct
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar el almacenamiento de imágenes para juegos y usuarios.
 * Maneja operaciones de carga, almacenamiento y recuperación de archivos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    // === Constantes y Configuración ===
    private final FileStorageProperties storageProperties;
    private static final String DEFAULT_EXTENSION = "jpg";
    private static final String DEFAULT_FILENAME_PREFIX = "imagen_sin_nombre";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    /**
     * Tipos de imagen soportados.
     * Determinan la ubicación de almacenamiento.
     */
    public enum ImageType {
        GAME,    // Imágenes de juegos
        USER,    // Imágenes de usuarios
        DEFAULT  // Tipo por defecto
    }

    /**
     * Inicializa los directorios necesarios.
     * Se ejecuta automáticamente después de la construcción del bean.
     */
    @PostConstruct
    public void init() {
        try {
            Path rootLocation = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);

            Path userImageLocation = Paths.get(storageProperties.getUserImagesPath()).toAbsolutePath().normalize();
            Files.createDirectories(userImageLocation);

            Path gameImageLocation = Paths.get(storageProperties.getGameImagesPath()).toAbsolutePath().normalize();
            Files.createDirectories(gameImageLocation);

            log.info("Directorios de subida creados/verificados: Root -> {}, User -> {}, Game -> {}",
                    rootLocation, userImageLocation, gameImageLocation);
        } catch (Exception ex) {
            log.error("No se pudieron crear los directorios de subida.", ex);
            throw new FileStorageException("No se pudieron crear los directorios de subida. " + ex.getMessage(), ex);
        }
    }


    /**
     * Almacena una imagen desde un MultipartFile.
     * @param file Archivo a almacenar
     * @param type Tipo de imagen (GAME, USER)
     * @return Nombre único generado para el archivo
     * @throws FileStorageException si hay problemas al almacenar
     */
    public String storeImage(MultipartFile file, ImageType type) {
        if (file == null || file.isEmpty()) {
            log.warn("Intento de almacenar un archivo nulo o vacío para el tipo {}", type);
            throw new FileStorageException("No se puede almacenar un archivo vacío.");
        }

        validateFileSize(file.getSize());

        String rawOriginalFilename = file.getOriginalFilename();
        String originalFilename = StringUtils.cleanPath(rawOriginalFilename == null ? "" : rawOriginalFilename);
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = DEFAULT_FILENAME_PREFIX + "." + DEFAULT_EXTENSION;
        }

        String extension = getFileExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            extension = DEFAULT_EXTENSION;
        }

        validateFileExtension(extension);

        try (InputStream inputStream = file.getInputStream()) {
            return storeImageCore(inputStream, type, extension, originalFilename);
        } catch (IOException e) {
            log.error("Error al leer el archivo de entrada para el tipo {}: {}", type, originalFilename, e);
            throw new FileStorageException("Error al leer el archivo de entrada: " + originalFilename, e);
        }
    }

    /**
     * Almacena una imagen desde un array de bytes.
     * @param imageBytes Datos de la imagen
     * @param type Tipo de imagen
     * @param providedExtension Extensión del archivo
     * @return Nombre único generado para el archivo
     * @throws FileStorageException si hay problemas al almacenar
     */
    public String storeImage(byte[] imageBytes, ImageType type, String providedExtension) {
        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("Intento de almacenar una imagen vacía (desde bytes) para el tipo {}", type);
            throw new FileStorageException("No se puede almacenar una imagen vacía.");
        }

        validateFileSize(imageBytes.length);

        String extension = sanitizeExtension(providedExtension);
        validateFileExtension(extension);

        String newFilename = generateUniqueFilename(extension);
        Path targetLocation = getTargetLocation(type, newFilename);

        try {
            Files.createDirectories(targetLocation.getParent());
            Files.write(targetLocation, imageBytes);
            log.info("Imagen (desde bytes) almacenada exitosamente: {} (Tipo: {}) en path: {}", newFilename, type,
                    targetLocation);
            return newFilename;
        } catch (IOException e) {
            log.error("Error al almacenar la imagen (desde bytes) {} para el tipo {} en path {}:", newFilename, type,
                    targetLocation, e);
            throw new FileStorageException("Error al almacenar la imagen (desde bytes) " + newFilename, e);
        }
    }

    // === Métodos Privados de Almacenamiento ===

    /**
     * Núcleo del proceso de almacenamiento.
     * Maneja la escritura efectiva del archivo.
     */
    private String storeImageCore(InputStream inputStream, ImageType type, String extension,
            String originalFilenameForLog) throws IOException {
        String newFilename = generateUniqueFilename(extension);
        Path targetLocation = getTargetLocation(type, newFilename);

        Files.createDirectories(targetLocation.getParent());
        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Imagen '{}' almacenada como '{}' (Tipo: {}) en path: {}", originalFilenameForLog, newFilename, type,
                targetLocation);
        return newFilename;
    }

    // === Validaciones ===

    /**
     * Valida el tamaño del archivo contra límites configurados
     */
    private void validateFileSize(long fileSize) {
        long maxSizeInBytes = storageProperties.getMaxSize();
        if (fileSize > maxSizeInBytes) {
            long maxSizeInMB = maxSizeInBytes / (1024 * 1024);
            String message = String.format("El tamaño del archivo (%d bytes) excede el límite permitido de %dMB.", fileSize, maxSizeInMB);
            log.warn(message);
            throw new FileStorageException(message);
        }
    }

    /**
     * Valida que la extensión esté permitida
     */
    private void validateFileExtension(String extension) {
        if (!storageProperties.isExtensionAllowed(extension)) {
            String allowedExtensionsString = "desconocidas (revisar configuración)";
            if (storageProperties.getAllowedExtensions() != null && !storageProperties.getAllowedExtensions().isEmpty()) {
                allowedExtensionsString = String.join(", ", storageProperties.getAllowedExtensions());
            }
            String message = String.format("Extensión de archivo no permitida: '%s'. Permitidas: [%s]", extension, allowedExtensionsString);
            log.warn(message);
            throw new FileStorageException(message);
        }
    }

    // === Utilidades ===

    /**
     * Limpia y normaliza la extensión del archivo
     */
    private String sanitizeExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return DEFAULT_EXTENSION;
        }
        String sanitized = extension.startsWith(".") ? extension.substring(1) : extension;
        return sanitized.toLowerCase();
    }

    /**
     * Carga una imagen como recurso
     * @param filename Nombre del archivo
     * @param type Tipo de imagen
     * @return Resource que representa la imagen
     * @throws MyFileNotFoundException si no se encuentra
     */
    public Resource loadImageAsResource(String filename, ImageType type) {
        if (!StringUtils.hasText(filename)) {
            log.warn("Intento de cargar imagen con nombre nulo o vacío (Tipo: {})", type);
            throw new MyFileNotFoundException("El nombre del archivo no puede ser nulo o vacío.");
        }
        try {
            Path filePath = getTargetLocation(type, StringUtils.cleanPath(filename));
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.debug("Recurso cargado: {} (Tipo: {}) desde path: {}", filename, type, filePath);
                return resource;
            } else {
                log.warn("Intento de cargar archivo no existente o no legible: {} (Tipo: {}) en path: {}", filename,
                        type, filePath);
                throw new MyFileNotFoundException(
                        "Imagen no encontrada o no accesible: " + filename + " (Tipo: " + type + ")");
            }
        } catch (MalformedURLException e) {
            log.error("URL mal formada para el archivo: {} (Tipo: {})", filename, type, e);
            throw new MyFileNotFoundException("Error al acceder a la imagen (URL mal formada): " + filename, e);
        }
    }

    /**
     * Elimina una imagen del sistema
     * @return true si se eliminó, false si no existía
     */
    public boolean deleteImage(String filename, ImageType type) {
        if (!StringUtils.hasText(filename)) {
            log.warn("Intento de eliminar imagen con nombre nulo o vacío (Tipo: {})", type);
            return false;
        }
        try {
            Path filePath = getTargetLocation(type, StringUtils.cleanPath(filename));
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Imagen eliminada: {} (Tipo: {}) desde path: {}", filename, type, filePath);
            } else {
                log.warn("No se pudo eliminar la imagen (podría no existir): {} (Tipo: {}) en path: {}", filename, type,
                        filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Error al eliminar la imagen {} (Tipo: {}): {}", filename, type, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Genera un nombre único para el archivo
     * Usa timestamp y UUID
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * Extrae la extensión de un nombre de archivo
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Determina la ubicación absoluta de almacenamiento
     */
    private Path getTargetLocation(ImageType type, String filename) {
        String specificDirectoryPathString = switch (type) {
            case GAME -> storageProperties.getGameImagesPath(); // ej. "uploads/games" o "/var/www/uploads/games"
            case USER -> storageProperties.getUserImagesPath(); // ej. "uploads/users" o "/var/www/uploads/users"
            default -> {
                log.warn("Tipo de imagen desconocido: {}. Usando subdirectorio 'default' en el directorio raíz de subida.", type);
                String rootDir = storageProperties.getUploadDir();
                if (!StringUtils.hasText(rootDir)) {
                    log.error("Directorio de subida raíz no está configurado en FileStorageProperties.");
                    throw new FileStorageException("Directorio de subida raíz no configurado.");
                }
                yield Paths.get(rootDir, "default").toString(); // Devuelve la ruta al directorio "default"
            }
        };

        if (!StringUtils.hasText(specificDirectoryPathString)) {
            log.error("La ruta base para el tipo de imagen {} no está configurada o es inválida.", type);
            throw new FileStorageException("La ruta de almacenamiento para el tipo " + type + " no está configurada.");
        }

        // Construye la ruta absoluta al archivo
        return Paths.get(specificDirectoryPathString).resolve(StringUtils.cleanPath(filename)).toAbsolutePath().normalize();
    }

    /**
     * Obtiene la ruta relativa para almacenamiento en BD
     */
    public String getLocalPath(ImageType type, String fileName) {

        Path rootUploadPath = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path subDirectoryFullPath;

        switch (type) {
            case USER:
                subDirectoryFullPath = Paths.get(storageProperties.getUserImagesPath()).toAbsolutePath().normalize();
                break;
            case GAME:
                subDirectoryFullPath = Paths.get(storageProperties.getGameImagesPath()).toAbsolutePath().normalize();
                break;
            default:
                return Paths.get("default", StringUtils.cleanPath(fileName)).toString().replace("\\", "/");
        }


        Path relativePathToSubDir;
        try {
            relativePathToSubDir = rootUploadPath.relativize(subDirectoryFullPath);
        } catch (IllegalArgumentException e) {
            log.warn("No se pudo relativizar la ruta del subdirectorio '{}' contra la raíz '{}'. Usando el nombre del último directorio.", subDirectoryFullPath, rootUploadPath);
            relativePathToSubDir = subDirectoryFullPath.getFileName();
            if (relativePathToSubDir == null) {
                relativePathToSubDir = Paths.get("");
            }
        }
        
        return relativePathToSubDir.resolve(StringUtils.cleanPath(fileName)).toString().replace("\\", "/");
    }
}
