package org.project.group5.gamelend.service;

import org.project.group5.gamelend.config.FileStorageProperties;
import org.project.group5.gamelend.exception.FileStorageException;
import org.project.group5.gamelend.exception.MyFileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Servicio para almacenar y recuperar archivos del sistema de archivos
 */
@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private final FileStorageProperties fileStorageProperties;

    /**
     * Constructor para inyección de dependencias
     * @param fileStorageProperties Propiedades de configuración de almacenamiento
     */
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    /**
     * Almacena un archivo en el sistema de archivos
     * @param fileBytes Contenido del archivo en bytes
     * @param fileName Nombre del archivo (opcional, se genera UUID si es null)
     * @return Nombre del archivo almacenado
     * @throws FileStorageException Si ocurre un error al almacenar el archivo
     */
    public String storeFile(byte[] fileBytes, String fileName) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new FileStorageException("El archivo está vacío");
        }

        if (fileName == null || fileName.isEmpty()) {
            fileName = UUID.randomUUID().toString();
        }
        
        // Sanitizar el nombre del archivo para prevenir path traversal
        fileName = Paths.get(fileName).getFileName().toString();

        Path fileStorageLocation = getFileStorageLocation("DOCUMENTOS");
        Path targetLocation = fileStorageLocation.resolve(fileName);

        try {
            Files.write(targetLocation, fileBytes);
            logger.info("Archivo almacenado exitosamente: {}", fileName);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("No se pudo almacenar el archivo " + fileName, e);
        }
    }

    /**
     * Determina el nombre de la carpeta basado en la extensión del archivo
     * @param completeFileName Nombre completo del archivo con extensión
     * @return Nombre de la carpeta (extensión en mayúsculas)
     */
    private String getFolderName(String completeFileName) {
        if (completeFileName == null || !completeFileName.contains(".")) {
            return "OTROS"; // Carpeta predeterminada si no hay extensión
        }
        String extension = completeFileName.substring(completeFileName.lastIndexOf("."));
        return extension.replace(".", "").toUpperCase();
    }

    /**
     * Crea y retorna la ubicación de almacenamiento del archivo
     * @param folderName Nombre de la carpeta
     * @return Path al directorio de almacenamiento
     * @throws FileStorageException Si no se puede crear el directorio
     */
    private Path getFileStorageLocation(String folderName) {
        if (folderName == null) {
            throw new IllegalArgumentException("El nombre de la carpeta no puede ser null");
        }
        
        // Ubicación base para el almacenamiento
        Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir() + "/" + folderName)
                                       .toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            return fileStorageLocation;
        } catch (IOException e) {
            throw new FileStorageException("No se pudo crear el directorio: " + fileStorageLocation, e);
        }
    }

    /**
     * Carga un archivo como un recurso
     * @param completeFileName Nombre completo del archivo
     * @return Resource que representa el archivo
     * @throws MyFileNotFoundException Si el archivo no existe
     */
    public Resource loadResource(String completeFileName) {
        // Validar entrada
        if (completeFileName == null || completeFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede ser null o vacío");
        }
        
        // Sanitizar el nombre del archivo
        completeFileName = Paths.get(completeFileName).getFileName().toString();
        
        // Obtener el nombre de la carpeta basado en la extensión del archivo
        String folderName = getFolderName(completeFileName);
        logger.debug("Nombre de la carpeta: {}", folderName);

        Path fileStorageLocation = getFileStorageLocation(folderName);
        Path path = fileStorageLocation.resolve(completeFileName).normalize();

        logger.debug("Intentando cargar el recurso desde: {}", path);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("Archivo no encontrado: " + completeFileName);
            }
        } catch (MalformedURLException e) {
            throw new MyFileNotFoundException("Ha ocurrido un error al intentar acceder al archivo: " + completeFileName, e);
        }
    }

    /**
     * Elimina un archivo del sistema de archivos
     * @param completeFileName Nombre completo del archivo
     * @return true si el archivo fue eliminado, false en caso contrario
     * @throws FileStorageException Si ocurre un error al eliminar el archivo
     */
    public boolean deleteFile(String completeFileName) {
        if (completeFileName == null || completeFileName.trim().isEmpty()) {
            return false;
        }
        
        // Sanitizar el nombre del archivo
        completeFileName = Paths.get(completeFileName).getFileName().toString();
        
        try {
            Path targetLocation = getFileStorageLocation(getFolderName(completeFileName))
                                 .resolve(completeFileName).normalize();
            return Files.deleteIfExists(targetLocation);
        } catch (IOException e) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + completeFileName, e);
        }
    }
}

