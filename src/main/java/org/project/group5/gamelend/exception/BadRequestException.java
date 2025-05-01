package org.project.group5.gamelend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando una solicitud contiene parámetros inválidos o
 * faltantes.
 * Resulta en una respuesta HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea una nueva excepción de solicitud incorrecta con el mensaje especificado.
     *
     * @param message mensaje detallado de error
     */
    public BadRequestException(String message) {
        super(message);
    }
}