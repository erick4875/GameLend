package org.project.group5.gamelend.exception;

import org.hibernate.JDBCException;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador de excepciones específicas con alta prioridad
 * 
 * Este controlador intercepta excepciones específicas del dominio y las transforma
 * en respuestas consistentes. Tiene mayor prioridad que GenericExceptionHandler.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpecificExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SpecificExceptionHandler.class);

    /**
     * Maneja excepciones de base de datos JDBC/SQL
     */
    @ExceptionHandler(JDBCException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RespuestaGeneral<String>> handleSqlException(JDBCException ex) {
        logger.error("Error de base de datos: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION,
                RespuestaGlobal.RESP_ERROR,
                "Error de base de datos: " + ex.getSQLException().getMessage(),
                null
            )
        );
    }

    /**
     * Maneja errores de validación de parámetros de método
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RespuestaGeneral<String>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Error de validación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION,
                RespuestaGlobal.RESP_ERROR,
                "Datos inválidos: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                null
            )
        );
    }

    /**
     * Maneja excepciones específicas de almacenamiento de archivos
     */
    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RespuestaGeneral<String>> handleFileStorageException(FileStorageException ex) {
        logger.error("Error en almacenamiento de archivos: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION,
                RespuestaGlobal.RESP_ERROR,
                "Error al procesar el archivo: " + ex.getMessage(),
                null
            )
        );
    }

    /**
     * Maneja excepciones de archivo no encontrado
     */
    @ExceptionHandler(MyFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<RespuestaGeneral<String>> handleFileNotFoundException(MyFileNotFoundException ex) {
        logger.warn("Archivo no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION,
                RespuestaGlobal.RESP_ERROR,
                "Archivo no encontrado: " + ex.getMessage(),
                null
            )
        );
    }

    /**
     * Maneja excepciones de usuario no encontrado
     */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<RespuestaGeneral<String>> handleUsuarioNoEncontradoException(UsuarioNoEncontradoException ex) {
        logger.warn("Usuario no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION,
                RespuestaGlobal.RESP_ERROR,
                ex.getMessage(),
                null
            )
        );
    }
}
