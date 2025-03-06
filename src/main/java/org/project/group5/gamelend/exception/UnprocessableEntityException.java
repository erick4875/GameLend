package org.project.group5.gamelend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para solicitudes semánticamente incorrectas (HTTP 422)
 * 
 * Esta excepción se lanza cuando una solicitud está sintácticamente correcta
 * pero contiene errores semánticos que impiden su procesamiento. Por ejemplo:
 * - Datos de entidad que no cumplen reglas de negocio
 * - Solicitudes con parámetros válidos pero lógicamente incompatibles
 * - Operaciones que no pueden completarse por el estado actual del sistema
 */
@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityException extends RuntimeException {

    /** Identificador de versión para serialización */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto
     */
    public UnprocessableEntityException() {
        super();
    }

    /**
     * Constructor con mensaje y causa, con control de supresión y rastreo de pila
     *
     * @param message            Mensaje descriptivo del error
     * @param cause              Excepción que causó este error
     * @param enableSuppression  Activa o desactiva la supresión de excepciones
     * @param writableStackTrace Activa o desactiva la escritura del rastreo de pila
     */
    public UnprocessableEntityException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param message Mensaje descriptivo del error
     * @param cause   Excepción que causó este error
     */
    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor con mensaje
     *
     * @param message Mensaje descriptivo del error
     */
    public UnprocessableEntityException(String message) {
        super(message);
    }

    /**
     * Constructor con causa
     *
     * @param cause Excepción que causó este error
     */
    public UnprocessableEntityException(Throwable cause) {
        super(cause);
    }
}
