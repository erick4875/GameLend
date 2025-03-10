package org.project.group5.gamelend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.PrestamoDTO;
import org.project.group5.gamelend.dto.PrestamoDevolucionDTO;
import org.project.group5.gamelend.dto.PrestamoResponseDTO;
import org.project.group5.gamelend.entity.Prestamo;
import org.project.group5.gamelend.service.PrestamoService;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar operaciones con préstamos
 */
@RestController
@RequestMapping("api/prestamos")
public class PrestamoController {
    private static final Logger logger = LoggerFactory.getLogger(PrestamoController.class);
    
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /**
     * Crea un nuevo préstamo
     * @param prestamoDTO Datos del préstamo a crear
     * @return Préstamo creado
     */
    @PostMapping
    public ResponseEntity<RespuestaGeneral<PrestamoResponseDTO>> createPrestamo(@RequestBody PrestamoDTO prestamoDTO) {
        logger.info("Creando nuevo préstamo");
        
        if (prestamoDTO == null) {
            logger.warn("Datos de préstamo nulos");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Los datos del préstamo no pueden ser nulos",
                    null
                )
            );
        }
        
        try {
            // Convertir DTO a entidad
            Prestamo prestamo = prestamoDTO.toEntity();
            
            // Guardar usando servicio existente
            RespuestaGeneral<Prestamo> respuesta = prestamoService.savePrestamo(prestamo);
            
            if (respuesta.isExito()) {
                // Convertir entidad guardada a DTO de respuesta
                PrestamoResponseDTO responseDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
                
                logger.info("Préstamo creado correctamente con ID: {}", responseDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        responseDTO
                    )
                );
            } else {
                logger.warn("Error al crear préstamo: {}", respuesta.getMensaje());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al crear préstamo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al crear el préstamo: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Obtiene todos los préstamos
     * @return Lista de préstamos
     */
    @GetMapping
    public ResponseEntity<RespuestaGeneral<List<PrestamoResponseDTO>>> getAllPrestamos() {
        logger.info("Solicitando lista de préstamos");
        
        try {
            RespuestaGeneral<List<Prestamo>> respuesta = prestamoService.getAllPrestamos();
            
            if (respuesta.isExito()) {
                // Convertir lista de entidades a lista de DTOs
                List<PrestamoResponseDTO> prestamosDTO = respuesta.getCuerpo()
                    .stream()
                    .map(PrestamoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
                
                return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        prestamosDTO
                    )
                );
            } else if (respuesta.getRespuesta() == RespuestaGlobal.RESP_ADV) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        List.of()
                    )
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al obtener préstamos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al obtener préstamos: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Obtiene un préstamo por su ID
     * @param id ID del préstamo
     * @return Préstamo encontrado o mensaje de error
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<PrestamoResponseDTO>> getPrestamoById(@PathVariable Long id) {
        logger.info("Buscando préstamo con ID: {}", id);
        
        if (id == null) {
            logger.warn("ID de préstamo nulo");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "ID de préstamo no puede ser nulo",
                    null
                )
            );
        }
        
        try {
            RespuestaGeneral<Prestamo> respuesta = prestamoService.getPrestamoById(id);
            
            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                PrestamoResponseDTO prestamoDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
                
                return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        prestamoDTO
                    )
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al buscar préstamo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al buscar el préstamo: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Elimina un préstamo por su ID
     * @param id ID del préstamo a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<String>> deletePrestamo(@PathVariable Long id) {
        logger.info("Eliminando préstamo con ID: {}", id);
        
        if (id == null) {
            logger.warn("ID de préstamo nulo");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "ID de préstamo no puede ser nulo",
                    null
                )
            );
        }
        
        try {
            RespuestaGeneral<String> respuesta = prestamoService.deletePrestamo(id);
            
            if (respuesta.isExito()) {
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar préstamo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al eliminar el préstamo: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Actualiza un préstamo existente
     * @param id ID del préstamo a actualizar
     * @param prestamoDTO Nuevos datos del préstamo
     * @return Préstamo actualizado o mensaje de error
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<PrestamoResponseDTO>> updatePrestamo(
            @PathVariable Long id, @RequestBody PrestamoDTO prestamoDTO) {
        logger.info("Actualizando préstamo con ID: {}", id);
        
        if (id == null || prestamoDTO == null) {
            logger.warn("ID o datos de préstamo nulos");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "ID y datos del préstamo no pueden ser nulos",
                    null
                )
            );
        }
        
        try {
            // Convertir DTO a entidad
            Prestamo prestamo = prestamoDTO.toEntity();
            prestamo.setId(id); // Asegurar que se actualiza el préstamo correcto
            
            RespuestaGeneral<Prestamo> respuesta = prestamoService.updatePrestamo(id, prestamo);
            
            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                PrestamoResponseDTO responseDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
                
                return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        responseDTO
                    )
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al actualizar préstamo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al actualizar el préstamo: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Registra la devolución de un préstamo
     * 
     * @param id ID del préstamo a devolver
     * @param devolucionDTO Datos de la devolución (fecha)
     * @return Préstamo actualizado con la devolución registrada
     */
    @PutMapping("/{id}/devolver")
    public ResponseEntity<RespuestaGeneral<PrestamoResponseDTO>> devolverPrestamo(
            @PathVariable Long id,
            @RequestBody PrestamoDevolucionDTO devolucionDTO) {
        
        logger.info("Registrando devolución del préstamo con ID: {}", id);
        
        // Validar que el DTO no sea nulo
        if (devolucionDTO == null) {
            logger.warn("Datos de devolución nulos para préstamo ID: {}", id);
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Los datos de devolución no pueden ser nulos",
                    null
                )
            );
        }
        
        try {
            // Obtener el préstamo existente
            RespuestaGeneral<Prestamo> respuestaPrestamo = prestamoService.getPrestamoById(id);
            
            if (!respuestaPrestamo.isExito() || respuestaPrestamo.getCuerpo() == null) {
                logger.warn("No se encontró el préstamo con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        respuestaPrestamo.getMensaje(),
                        null
                    )
                );
            }
            
            // Actualizar la fecha de devolución
            Prestamo prestamo = respuestaPrestamo.getCuerpo();
            
            // Conversión correcta de String a LocalDateTime
            try {
                // Usar el método que convierte a LocalDateTime en lugar de getString
                prestamo.setFechaDevolucion(devolucionDTO.getFechaDevolucionAsDateTime());
            } catch (Exception e) {
                logger.error("Error al convertir la fecha de devolución: {}", e.getMessage(), e);
                return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Formato de fecha incorrecto: " + e.getMessage(),
                        null
                    )
                );
            }
            
            // Procesar la devolución y obtener respuesta DTO
            RespuestaGeneral<PrestamoResponseDTO> respuesta = prestamoService.devolverPrestamoDTO(prestamo);
            
            if (!respuesta.isExito()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
            }
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            logger.error("Error al devolver el préstamo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al devolver el préstamo: " + e.getMessage(),
                    null
                )
            );
        }
    }
}
