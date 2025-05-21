package org.project.group5.gamelend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
 * Servicio para gestionar el almacenamiento de imágenes para juegos y usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageProperties storageProperties;
    private static final String DEFAULT_EXTENSION = "jpg";
    private static final String DEFAULT_FILENAME_PREFIX = "imagen_sin_nombre";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    /**
     * Tipos de imagen soportados (determinan la ubicación de almacenamiento).
     */
    public enum ImageType {
        GAME,
        USER
    }

    /**
     * Almacena una imagen recibida como MultipartFile.
     * Valida tamaño y extensión antes de guardar.
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
     * Almacena una imagen recibida como array de bytes.
     * Valida tamaño y extensión antes de guardar.
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

    /**
     * Lógica central para almacenar una imagen desde un InputStream.
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

    /**
     * Valida el tamaño del archivo.
     * Lanza excepción si excede el máximo permitido.
     */
    private void validateFileSize(long fileSize) {
        long maxSizeInBytes = storageProperties.getMaxSize();
        if (fileSize > maxSizeInBytes) {
            long maxSizeInMB = maxSizeInBytes / (1024 * 1024);
            String message = "El tamaño del archivo (%d bytes) excede el límite permitido de %dMB.".formatted(fileSize, maxSizeInMB);
            log.warn(message);
            throw new FileStorageException(message);
        }
    }

    /**
     * Valida la extensión del archivo.
     * Lanza excepción si no está permitida.
     */
    private void validateFileExtension(String extension) {
        if (!storageProperties.isExtensionAllowed(extension)) {
            String allowedExtensionsString = "desconocidas (revisar configuración)";
            if (storageProperties.getAllowedExtensions() != null && !storageProperties.getAllowedExtensions().isEmpty()) {
                allowedExtensionsString = String.join(", ", storageProperties.getAllowedExtensions());
            }
            String message = "Extensión de archivo no permitida: '%s'. Permitidas: [%s]".formatted(extension, allowedExtensionsString);
            log.warn(message);
            throw new FileStorageException(message);
        }
    }

    /**
     * Limpia y normaliza la extensión del archivo.
     */
    private String sanitizeExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return DEFAULT_EXTENSION;
        }
        String sanitized = extension.startsWith(".") ? extension.substring(1) : extension;
        return sanitized.toLowerCase();
    }

    /**
     * Carga una imagen como recurso.
     * 
     * @param filename Nombre del archivo.
     * @param type     Tipo de imagen (GAME o USER).
     * @return Recurso que representa la imagen.
     * @throws MyFileNotFoundException Si la imagen no existe o no se puede acceder.
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
     * Elimina una imagen del sistema de archivos.
     * 
     * @param filename Nombre del archivo.
     * @param type     Tipo de imagen (GAME o USER).
     * @return true si la imagen fue eliminada, false en caso contrario.
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
     * Genera un nombre de archivo único usando timestamp y UUID.
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * Obtiene la extensión de un nombre de archivo.
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
     * Determina la ubicación para almacenar un archivo según su tipo.
     */
    private Path getTargetLocation(ImageType type, String filename) {
        String basePathString = switch (type) {
            case GAME -> storageProperties.getGameImagesPath();
            case USER -> storageProperties.getUserImagesPath();
            default -> {
                log.warn("Tipo de imagen desconocido: {}. Intentando usar directorio de subida por defecto.", type);
                String defaultPath = storageProperties.getUploadDir();
                if (!StringUtils.hasText(defaultPath)) {
                    log.error(
                            "Tipo de imagen no soportado ({}) y no hay directorio de subida por defecto configurado en FileStorageProperties.",
                            type);
                    throw new FileStorageException(
                            "Tipo de imagen no soportado y no hay directorio de subida por defecto.");
                }
                yield defaultPath;
            }
        };

        if (!StringUtils.hasText(basePathString)) {
            log.error("La ruta base para el tipo de imagen {} no está configurada en FileStorageProperties.", type);
            throw new FileStorageException("La ruta de almacenamiento para el tipo " + type + " no está configurada.");
        }

        Path basePath = Path.of(basePathString).toAbsolutePath().normalize();
        return basePath.resolve(StringUtils.cleanPath(filename)).normalize();
    }
}
