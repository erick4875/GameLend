package org.project.group5.gamelend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción que se lanza cuando un archivo solicitado no ha sido encontrado
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyFileNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto
     */
    public MyFileNotFoundException() {
        super("Archivo no encontrado");
    }

    /**
     * Constructor con mensaje personalizado
     * 
     * @param message Mensaje de error
     */
    public MyFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con causa original
     * 
     * @param cause Causa original del error
     */
    public MyFileNotFoundException(Throwable cause) {
        super("Archivo no encontrado", cause);
    }

    /**
     * Constructor con mensaje personalizado y causa original
     * 
     * @param message Mensaje de error
     * @param cause Causa original del error
     */
    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
