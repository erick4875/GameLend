package org.project.group5.gamelend.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción que se lanza cuando ocurren errores relacionados con el
 * almacenamiento de archivos
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto
     */
    public FileStorageException() {
        super("Error en el almacenamiento de archivos");
    }

    /**
     * Constructor con mensaje personalizado
     * 
     * @param message Mensaje de error
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructor con causa original
     * 
     * @param cause Causa original del error
     */
    public FileStorageException(Throwable cause) {
        super("Error en el almacenamiento de archivos", cause);
    }

    /**
     * Constructor con mensaje personalizado y causa original
     * 
     * @param message Mensaje de error
     * @param cause   Causa original del error
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
