package org.project.group5.gamelend.exception;

/**
 * Excepción personalizada para errores relacionados con el almacenamiento de archivos.
 * 
 * Esta excepción se lanza cuando ocurren problemas al crear, leer, actualizar o eliminar
 * archivos en el sistema de almacenamiento, como permisos insuficientes, errores de I/O,
 * o problemas de configuración del almacenamiento.
 */
public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Crea una nueva excepción con el mensaje de error especificado.
     * 
     * @param message Descripción del error
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con el mensaje y la causa raíz especificados.
     * 
     * @param message Descripción del error
     * @param cause Excepción original que causó este error
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
