package org.project.group5.gamelend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.JuegoResponseDTO;
import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.repository.JuegoRepository;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de juegos en la aplicación
 * Proporciona métodos para crear, leer, actualizar y eliminar juegos
 */
@Service
public class JuegoService {
    private static final Logger logger = LoggerFactory.getLogger(JuegoService.class);
    private final JuegoRepository juegoRepository;

    // Inyección de dependencia del repositorio
    public JuegoService(JuegoRepository juegoRepository) {
        this.juegoRepository = juegoRepository;
    }

    /**
     * Obtiene todos los juegos registrados
     * @return Respuesta con la lista de juegos o mensaje de advertencia si no hay juegos
     */
    public RespuestaGeneral<List<Juego>> findAll() {
        try {
            List<Juego> juegos = juegoRepository.findAll();
            if (juegos.isEmpty()) {
                logger.info("No se encontraron juegos en la base de datos");
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO, 
                    RespuestaGlobal.RESP_ADV, 
                    "No se encontraron juegos", 
                    null
                );
            }
            
            logger.debug("Se encontraron {} juegos", juegos.size());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_OK, 
                RespuestaGlobal.OPER_CORRECTA, 
                juegos
            );
        } catch (Exception e) {
            logger.error("Error al obtener la lista de juegos: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Error al recuperar los juegos: " + e.getMessage(), 
                null
            );
        }
    }

    /**
     * Encuentra todos los juegos y los convierte a DTOs
     * 
     * @return Respuesta con lista de JuegoResponseDTO
     */
    public RespuestaGeneral<List<JuegoResponseDTO>> findAllDTO() {
        try {
            RespuestaGeneral<List<Juego>> respuestaEntidades = findAll();
            
            // Si hay error, mantenemos el mismo tipo de respuesta pero con el tipo genérico diferente
            if (!respuestaEntidades.isExito()) {
                return new RespuestaGeneral<>(
                    respuestaEntidades.getTipo(),
                    respuestaEntidades.getRespuesta(),
                    respuestaEntidades.getMensaje(),
                    null
                );
            }
            
            List<JuegoResponseDTO> juegosDTO = respuestaEntidades.getCuerpo()
                .stream()
                .map(JuegoResponseDTO::fromEntity)
                .collect(Collectors.toList());
                
            return new RespuestaGeneral<>(
                respuestaEntidades.getTipo(),
                respuestaEntidades.getRespuesta(),
                respuestaEntidades.getMensaje(),
                juegosDTO
            );
        } catch (Exception e) {
            logger.error("Error al obtener listado de juegos: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener juegos: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Busca un juego por su ID
     * @param id ID del juego a buscar
     * @return Respuesta con el juego encontrado o mensaje de error
     */
    public RespuestaGeneral<Juego> findById(Long id) {
        if (id == null) {
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "El ID del juego no puede ser nulo", 
                null
            );
        }
        
        try {
            Optional<Juego> juego = juegoRepository.findById(id);
            if (juego.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO, 
                    RespuestaGlobal.RESP_OK, 
                    RespuestaGlobal.OPER_CORRECTA, 
                    juego.get()
                );
            }
            
            logger.info("Juego con ID {} no encontrado", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Juego no encontrado", 
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar juego con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Error al buscar el juego: " + e.getMessage(), 
                null
            );
        }
    }

    /**
     * Guarda un nuevo juego en la base de datos
     * @param juego Juego a guardar
     * @return Respuesta con el juego guardado o mensaje de error
     */
    public RespuestaGeneral<Juego> save(Juego juego) {
        if (juego == null) {
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "El juego no puede ser nulo", 
                null
            );
        }

        try {
            Juego juegoGuardado = juegoRepository.save(juego);
            logger.info("Juego '{}' guardado correctamente con ID: {}", 
                        juego.getTitulo(), juegoGuardado.getId());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_OK, 
                RespuestaGlobal.OPER_CORRECTA, 
                juegoGuardado
            );
        } catch (Exception e) {
            logger.error("Error al guardar el juego: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Error al guardar el juego: " + e.getMessage(), 
                null
            );
        }
    }

    /**
     * Elimina un juego por su ID
     * @param id ID del juego a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    public RespuestaGeneral<String> deleteById(Long id) {
        if (id == null) {
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "El ID del juego no puede ser nulo", 
                null
            );
        }

        try {
            if (juegoRepository.existsById(id)) {
                juegoRepository.deleteById(id);
                logger.info("Juego con ID {} eliminado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO, 
                    RespuestaGlobal.RESP_OK, 
                    "Juego eliminado correctamente", 
                    null
                );
            }
            
            logger.info("Intento de eliminar un juego inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Juego no encontrado", 
                null
            );
        } catch (Exception e) {
            logger.error("Error al eliminar juego con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Error al eliminar el juego: " + e.getMessage(), 
                null
            );
        }
    }

    /**
     * Actualiza un juego existente
     * @param id ID del juego a actualizar
     * @param juegoActualizado Nuevos datos del juego
     * @return Respuesta con el juego actualizado o mensaje de error
     */
    public RespuestaGeneral<Juego> updateJuego(Long id, Juego juegoActualizado) {
        if (id == null || juegoActualizado == null) {
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "El ID y datos del juego no pueden ser nulos", 
                null
            );
        }

        try {
            Optional<Juego> juegoExistente = juegoRepository.findById(id);
            if (juegoExistente.isPresent()) {
                juegoActualizado.setId(id);
                Juego juegoGuardado = juegoRepository.save(juegoActualizado);
                logger.info("Juego con ID {} actualizado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO, 
                    RespuestaGlobal.RESP_OK, 
                    RespuestaGlobal.OPER_CORRECTA, 
                    juegoGuardado
                );
            }
            
            logger.info("Intento de actualizar un juego inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Juego no encontrado", 
                null
            );
        } catch (Exception e) {
            logger.error("Error al actualizar juego con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO, 
                RespuestaGlobal.RESP_ERROR, 
                "Error al actualizar el juego: " + e.getMessage(), 
                null
            );
        }
    }

    /**
     * Busca juegos con imágenes asociados a un usuario específico
     * @param usuarioId ID del usuario
     * @return Respuesta con la lista de juegos que tienen imágenes
     */
    public RespuestaGeneral<List<Juego>> findByUsuarioIdWithImages(Long usuarioId) {
        logger.info("Buscando juegos con imágenes para usuario con ID: {}", usuarioId);
        
        if (usuarioId == null) {
            logger.warn("ID de usuario nulo");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de usuario no puede ser nulo",
                null
            );
        }
        
        try {
            // Asumiendo que tienes un método en el repositorio para esto
            // Si no lo tienes, necesitarás crearlo también
            List<Juego> juegos = juegoRepository.findByUsuarioIdAndImagenIsNotNull(usuarioId);
            
            if (juegos.isEmpty()) {
                logger.info("No se encontraron juegos con imágenes para el usuario con ID: {}", usuarioId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "El usuario no tiene juegos con imágenes",
                    null
                );
            }
            
            logger.debug("Se encontraron {} juegos con imágenes para el usuario con ID: {}", juegos.size(), usuarioId);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                juegos
            );
        } catch (Exception e) {
            logger.error("Error al buscar juegos con imágenes para usuario {}: {}", usuarioId, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al recuperar juegos: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Busca un juego por su título
     * @param titulo Título del juego a buscar
     * @return Respuesta con el juego encontrado o mensaje de error
     */
    public RespuestaGeneral<Juego> findByTitulo(String titulo) {
        logger.debug("Buscando juego con título: {}", titulo);
        
        if (titulo == null || titulo.trim().isEmpty()) {
            logger.warn("Título de juego nulo o vacío");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "El título del juego no puede ser nulo o vacío",
                null
            );
        }
        
        try {
            Optional<Juego> juegoOptional = juegoRepository.findByTitulo(titulo);
            if (juegoOptional.isPresent()) {
                logger.debug("Juego encontrado: {}", titulo);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    juegoOptional.get()
                );
            } else {
                logger.info("No se encontró juego con título: {}", titulo);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "No se encontró juego con título: " + titulo,
                    null
                );
            }
        } catch (Exception e) {
            logger.error("Error al buscar juego por título {}: {}", titulo, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar juego por título: " + e.getMessage(),
                null
            );
        }
    }
}


