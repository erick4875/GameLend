package org.project.group5.gamelend.exception;

import java.io.Serial;

/**
 * Excepción para cuando un usuario no es encontrado en la base de datos
 */
public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje de error
     * 
     * @param message Mensaje descriptivo del error
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje de error y causa
     * 
     * @param message Mensaje descriptivo del error
     * @param cause Causa original del error
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
