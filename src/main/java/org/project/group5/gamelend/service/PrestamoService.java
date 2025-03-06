package org.project.group5.gamelend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.PrestamoResponseDTO;
import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.entity.Prestamo;
import org.project.group5.gamelend.repository.JuegoRepository;
import org.project.group5.gamelend.repository.PrestamoRepository;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.project.group5.gamelend.util.RespuestaGlobal.Estado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de préstamos de juegos
 */
@Service
public class PrestamoService {
    private static final Logger logger = LoggerFactory.getLogger(PrestamoService.class);

    private final PrestamoRepository prestamoRepository;
    private final JuegoRepository juegoRepository;

    public PrestamoService(PrestamoRepository prestamoRepository, JuegoRepository juegoRepository) {
        this.juegoRepository = juegoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    /**
     * Crea un nuevo préstamo
     * @param prestamo Objeto préstamo a guardar
     * @return Respuesta con el préstamo guardado o mensaje de error
     */
    public RespuestaGeneral<Prestamo> savePrestamo(Prestamo prestamo) {
        if (prestamo == null) {
            logger.warn("Intento de guardar un préstamo nulo");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "El préstamo no puede ser nulo",
                null
            );
        }

        try {
            Prestamo saved = prestamoRepository.save(prestamo);
            logger.info("Préstamo guardado correctamente con ID: {}", saved.getId());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                saved
            );
        } catch (Exception e) {
            logger.error("Error al guardar el préstamo: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al guardar el préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene todos los préstamos registrados
     * @return Respuesta con la lista de préstamos o mensaje de advertencia
     */
    public RespuestaGeneral<List<Prestamo>> getAllPrestamos() {
        try {
            List<Prestamo> prestamos = prestamoRepository.findAll();
            if (prestamos.isEmpty()) {
                logger.info("No se encontraron préstamos");
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "No se encontraron préstamos",
                    null
                );
            }

            logger.debug("Se encontraron {} préstamos", prestamos.size());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                prestamos
            );
        } catch (Exception e) {
            logger.error("Error al obtener préstamos: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener préstamos: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un préstamo por su ID
     * @param id ID del préstamo
     * @return Respuesta con el préstamo o mensaje de error
     */
    public RespuestaGeneral<Prestamo> getPrestamoById(Long id) {
        if (id == null) {
            logger.warn("ID de préstamo nulo");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de préstamo no puede ser nulo",
                null
            );
        }

        try {
            Optional<Prestamo> prestamo = prestamoRepository.findById(id);
            if (prestamo.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    prestamo.get()
                );
            }
            
            logger.info("Préstamo con ID {} no encontrado", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Préstamo no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar préstamo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar el préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Elimina un préstamo por su ID
     * @param id ID del préstamo a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    public RespuestaGeneral<String> deletePrestamo(Long id) {
        if (id == null) {
            logger.warn("ID de préstamo nulo en intento de eliminación");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de préstamo no puede ser nulo",
                null
            );
        }

        try {
            if (prestamoRepository.existsById(id)) {
                prestamoRepository.deleteById(id);
                logger.info("Préstamo con ID {} eliminado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    "Préstamo eliminado correctamente",
                    null
                );
            }
            
            logger.info("Intento de eliminar un préstamo inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Préstamo no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al eliminar préstamo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al eliminar el préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Actualiza un préstamo existente
     * @param id ID del préstamo a actualizar
     * @param prestamo Nuevos datos del préstamo
     * @return Respuesta con el préstamo actualizado o mensaje de error
     */
    public RespuestaGeneral<Prestamo> updatePrestamo(Long id, Prestamo prestamo) {
        if (id == null || prestamo == null) {
            logger.warn("ID o datos de préstamo nulos en actualización");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID y datos del préstamo no pueden ser nulos",
                null
            );
        }

        try {
            if (prestamoRepository.existsById(id)) {
                prestamo.setId(id);
                Prestamo updated = prestamoRepository.save(prestamo);
                logger.info("Préstamo con ID {} actualizado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    updated
                );
            }
            
            logger.info("Intento de actualizar un préstamo inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Préstamo no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al actualizar préstamo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al actualizar el préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene todos los préstamos y los convierte a DTOs para la respuesta
     * @return Respuesta con la lista de DTOs de préstamos
     */
    public RespuestaGeneral<List<PrestamoResponseDTO>> getAllPrestamosDTO() {
        try {
            RespuestaGeneral<List<Prestamo>> respuestaEntidades = getAllPrestamos();
            
            if (!respuestaEntidades.isExito() || respuestaEntidades.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuestaEntidades.getTipo(),
                    respuestaEntidades.getRespuesta(),
                    respuestaEntidades.getMensaje(),
                    null
                );
            }
            
            List<PrestamoResponseDTO> prestamosDTO = respuestaEntidades.getCuerpo()
                .stream()
                .map(PrestamoResponseDTO::fromEntity)
                .collect(Collectors.toList());
            
            return new RespuestaGeneral<>(
                respuestaEntidades.getTipo(),
                respuestaEntidades.getRespuesta(),
                respuestaEntidades.getMensaje(),
                prestamosDTO
            );
        } catch (Exception e) {
            logger.error("Error al obtener DTOs de préstamos: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener préstamos: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un préstamo por su ID y lo convierte a DTO
     * @param id ID del préstamo
     * @return Respuesta con el DTO del préstamo
     */
    public RespuestaGeneral<PrestamoResponseDTO> getPrestamoByIdDTO(Long id) {
        try {
            RespuestaGeneral<Prestamo> respuesta = getPrestamoById(id);
            
            if (!respuesta.isExito() || respuesta.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuesta.getTipo(),
                    respuesta.getRespuesta(),
                    respuesta.getMensaje(),
                    null
                );
            }
            
            PrestamoResponseDTO prestamoDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuesta.getTipo(),
                respuesta.getRespuesta(),
                respuesta.getMensaje(),
                prestamoDTO
            );
        } catch (Exception e) {
            logger.error("Error al obtener DTO de préstamo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Guarda un préstamo y devuelve su DTO
     * @param prestamo Préstamo a guardar
     * @return Respuesta con el DTO del préstamo guardado
     */
    public RespuestaGeneral<PrestamoResponseDTO> savePrestamoDTO(Prestamo prestamo) {
        try {
            RespuestaGeneral<Prestamo> respuesta = savePrestamo(prestamo);
            
            if (!respuesta.isExito() || respuesta.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuesta.getTipo(),
                    respuesta.getRespuesta(),
                    respuesta.getMensaje(),
                    null
                );
            }
            
            PrestamoResponseDTO prestamoDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuesta.getTipo(),
                respuesta.getRespuesta(),
                respuesta.getMensaje(),
                prestamoDTO
            );
        } catch (Exception e) {
            logger.error("Error al guardar DTO de préstamo: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al guardar préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Actualiza un préstamo y devuelve su DTO
     * @param id ID del préstamo a actualizar
     * @param prestamo Nuevos datos del préstamo
     * @return Respuesta con el DTO del préstamo actualizado
     */
    public RespuestaGeneral<PrestamoResponseDTO> updatePrestamoDTO(Long id, Prestamo prestamo) {
        try {
            RespuestaGeneral<Prestamo> respuesta = updatePrestamo(id, prestamo);
            
            if (!respuesta.isExito() || respuesta.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuesta.getTipo(),
                    respuesta.getRespuesta(),
                    respuesta.getMensaje(),
                    null
                );
            }
            
            PrestamoResponseDTO prestamoDTO = PrestamoResponseDTO.fromEntity(respuesta.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuesta.getTipo(),
                respuesta.getRespuesta(),
                respuesta.getMensaje(),
                prestamoDTO
            );
        } catch (Exception e) {
            logger.error("Error al actualizar DTO de préstamo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al actualizar préstamo: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Registra la devolución de un préstamo y actualiza el estado del juego
     * 
     * @param prestamo El préstamo con la fecha de devolución actualizada
     * @return Respuesta con el préstamo actualizado o error
     */
    @Transactional
    public RespuestaGeneral<Prestamo> devolverPrestamo(Prestamo prestamo) {
        try {
            // Verificar que el préstamo existe
            if (!prestamoRepository.existsById(prestamo.getId())) {
                logger.warn("El préstamo con ID {} no existe", prestamo.getId());
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "El préstamo no existe",
                        null);
            }
            
            // Verificar que no haya sido devuelto ya
            Prestamo prestamoExistente = prestamoRepository.findById(prestamo.getId()).orElse(null);
            if (prestamoExistente != null && prestamoExistente.getFechaDevolucion() != null) {
                logger.warn("El préstamo con ID {} ya ha sido devuelto", prestamo.getId());
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "El préstamo ya ha sido devuelto",
                        prestamoExistente);
            }
            
            // Verificar que el juego existe
            Juego juego = prestamoExistente.getJuego();
            if (juego == null) {
                logger.warn("El préstamo con ID {} no tiene un juego asociado", prestamo.getId());
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "El préstamo no tiene un juego asociado",
                        null);
            }
            
            // Actualizar la fecha de devolución
            prestamoExistente.setFechaDevolucion(prestamo.getFechaDevolucion());
            Prestamo prestamoActualizado = prestamoRepository.save(prestamoExistente);
            
            // Actualizar el estado del juego a DISPONIBLE
            juego.setEstado(Estado.DISPONIBLE);
            juegoRepository.save(juego);
            
            logger.info("Préstamo con ID {} devuelto correctamente", prestamo.getId());
            return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    "Préstamo devuelto correctamente",
                    prestamoActualizado);
            
        } catch (Exception e) {
            logger.error("Error al devolver el préstamo: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al devolver el préstamo: " + e.getMessage(),
                    null);
        }
    }

    /**
     * Registra la devolución de un préstamo y devuelve su respuesta DTO
     * 
     * @param prestamo El préstamo con la fecha de devolución actualizada
     * @return Respuesta con el DTO del préstamo actualizado
     */
    public RespuestaGeneral<PrestamoResponseDTO> devolverPrestamoDTO(Prestamo prestamo) {
        // Primero ejecutamos la lógica principal que actualiza la entidad
        RespuestaGeneral<Prestamo> respuestaEntidad = devolverPrestamo(prestamo);
        
        if (!respuestaEntidad.isExito() || respuestaEntidad.getCuerpo() == null) {
            // Si hubo un error, retornamos el mismo error pero con tipo DTO
            return new RespuestaGeneral<>(
                respuestaEntidad.getTipo(),
                respuestaEntidad.getRespuesta(),
                respuestaEntidad.getMensaje(),
                null
            );
        }
        
        // Usar el método fromEntity de PrestamoResponseDTO O el DTOConverter
        // Opción 1: Usar PrestamoResponseDTO.fromEntity (método estático)
        PrestamoResponseDTO responseDTO = PrestamoResponseDTO.fromEntity(respuestaEntidad.getCuerpo());
        
        // Opción 2: Usar el DTOConverter (recomendado para consistencia)
        // PrestamoResponseDTO responseDTO = DTOConverter.convertToPrestamoResponseDTO(respuestaEntidad.getCuerpo());
        
        // Retornar respuesta exitosa con el DTO
        return new RespuestaGeneral<>(
            respuestaEntidad.getTipo(),
            respuestaEntidad.getRespuesta(),
            respuestaEntidad.getMensaje(),
            responseDTO
        );
    }
}
