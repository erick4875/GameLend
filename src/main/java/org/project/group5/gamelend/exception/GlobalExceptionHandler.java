package org.project.group5.gamelend.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones para la aplicación.
 * Intercepta excepciones y las convierte en respuestas HTTP con formato consistente.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recurso no encontrado
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja excepciones de solicitud incorrecta
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Solicitud incorrecta: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maneja excepciones de almacenamiento de archivos
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorage(FileStorageException ex) {
        log.error("Error de almacenamiento de archivos: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * Maneja excepciones de argumentos ilegales
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maneja excepciones de entrada/salida
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        log.error("Error de E/S: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar archivo: " + ex.getMessage());
    }

    /**
     * Maneja excepciones de tamaño de archivo excedido
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.warn("Tamaño de archivo excedido: {}", ex.getMessage());
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo excede el tamaño máximo permitido");
    }

    /**
     * Maneja excepciones de validación de argumentos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errors.put("message", "Error de validación");
        errors.put("details", validationErrors);
        
        log.warn("Error de validación: {}", validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    /**
     * Maneja excepciones de parámetros faltantes
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = "Parámetro requerido '%s' no está presente".formatted(ex.getParameterName());
        log.warn(message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * Maneja excepciones de tipo de argumento incorrecto
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = "desconocido";
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null) {
            typeName = requiredType.getSimpleName();
        }
        String message = "El parámetro '%s' debe ser de tipo '%s'".formatted(
                ex.getName(), typeName);
        log.warn(message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * Maneja excepciones de cuerpo de solicitud no legible
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de solicitud no legible: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Formato de solicitud inválido");
    }
    
    /**
     * Maneja excepciones de violación de integridad de datos
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Violación de integridad de datos: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "No se puede completar la operación debido a restricciones de datos");
    }
    
    /**
     * Maneja excepciones de acceso denegado
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción");
    }
    
    /**
     * Maneja excepciones de credenciales incorrectas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Credenciales incorrectas: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }

    /**
     * Maneja excepciones de tiempo de ejecución generales
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Error de tiempo de ejecución: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * Maneja cualquier otra excepción no contemplada
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servidor");
    }

    /**
     * Construye una respuesta de error con formato consistente
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
