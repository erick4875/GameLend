package org.project.group5.gamelend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un usuario solicitado no existe en el sistema
 * 
 * Esta excepción se utiliza cuando se intenta acceder, modificar o realizar
 * operaciones con un usuario que no se encuentra en la base de datos, ya sea
 * mediante su ID, email u otros identificadores.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsuarioNoEncontradoException extends RuntimeException {
    
    /** Identificador de versión para serialización */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje descriptivo
     * 
     * @param message Mensaje que describe la situación de error
     */
    public UsuarioNoEncontradoException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz
     * 
     * @param message Mensaje descriptivo del error
     * @param cause Excepción que causó este error
     */
    public UsuarioNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
