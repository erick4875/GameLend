package org.project.group5.gamelend.exception;

import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Manejador global de excepciones para la aplicación
 * 
 * Captura distintos tipos de excepciones y las convierte en respuestas
 * estructuradas para proporcionar información clara sobre los errores.
 */
@RestControllerAdvice
public class GenericExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionHandler.class);
    
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
     * Maneja excepciones de tamaño máximo de archivo excedido
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RespuestaGeneral<String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        logger.warn("Intento de subir archivo demasiado grande: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION, 
                RespuestaGlobal.RESP_ERROR,
                "El archivo excede el tamaño máximo permitido",
                null
            )
        );
    }
    
    /**
     * Maneja excepciones de recursos no encontrados
     */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<RespuestaGeneral<String>> handleResourceNotFoundException(RuntimeException ex) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION, 
                RespuestaGlobal.RESP_ERROR,
                ex.getMessage(),
                null
            )
        );
    }

    /**
     * Captura cualquier excepción no manejada específicamente
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RespuestaGeneral<String>> handleGenericException(Exception ex) {
        logger.error("Error no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_EXCEPTION, 
                RespuestaGlobal.RESP_ERROR,
                "Se produjo un error en el servidor: " + ex.getMessage(),
                null
            )
        );
    }
}
