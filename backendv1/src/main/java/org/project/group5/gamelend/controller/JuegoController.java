package org.project.group5.gamelend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.JuegoDTO;
import org.project.group5.gamelend.dto.JuegoResponseDTO;
import org.project.group5.gamelend.entity.Documento;
import org.project.group5.gamelend.entity.Juego;
import org.project.group5.gamelend.service.JuegoService;
import org.project.group5.gamelend.service.UsuarioService;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones relacionadas con juegos
 */
@RestController
@RequestMapping("api/juegos")
public class JuegoController {
    private static final Logger logger = LoggerFactory.getLogger(JuegoController.class);
    
    private final JuegoService juegoService;
    private final UsuarioService usuarioService;

    // Mantener solo un constructor que inicialice ambos servicios
    public JuegoController(JuegoService juegoService, UsuarioService usuarioService) {
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene todos los juegos registrados
     * @return Respuesta con la lista de juegos
     */
    @GetMapping
    public ResponseEntity<RespuestaGeneral<List<JuegoResponseDTO>>> getAllJuegos() {
        logger.info("Solicitando lista de juegos");
        
        RespuestaGeneral<List<JuegoResponseDTO>> respuesta = juegoService.findAllDTO();
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else if (respuesta.getRespuesta() == RespuestaGlobal.RESP_ADV) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    /**
     * Busca un juego por su título
     * @param titulo Título del juego
     * @return Respuesta con el juego encontrado
     */
    @GetMapping("/titulo/{titulo}")
    public ResponseEntity<RespuestaGeneral<JuegoResponseDTO>> getJuegoByTitulo(@PathVariable String titulo) {
        logger.info("Buscando juego con título: {}", titulo);
        
        if (titulo == null || titulo.trim().isEmpty()) {
            logger.warn("Título de juego nulo o vacío");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "El título del juego es requerido",
                    null
                )
            );
        }
        
        try {
            Juego juego = juegoService.findByTitulo(titulo).getCuerpo();
            
            if (juego != null) {
                JuegoResponseDTO juegoDTO = JuegoResponseDTO.fromEntity(juego);
                return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_OK,
                        RespuestaGlobal.OPER_CORRECTA,
                        juegoDTO
                    )
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Juego no encontrado",
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al buscar juego por título: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al buscar el juego: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Obtiene juegos por usuario que tienen imagen
     * @param usuarioId ID del usuario
     * @return Lista de juegos del usuario con imágenes
     */
    @GetMapping("/usuario/{usuarioId}/imagenes")
    public ResponseEntity<RespuestaGeneral<List<JuegoResponseDTO>>> getJuegosPorUsuarioYImagen(@PathVariable Long usuarioId) {
        logger.info("Buscando juegos con imágenes para usuario con ID: {}", usuarioId);
        
        if (usuarioId == null) {
            logger.warn("ID de usuario nulo");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "ID de usuario no puede ser nulo",
                    null
                )
            );
        }
        
        try {
            RespuestaGeneral<List<Juego>> respuestaEntidades = juegoService.findByUsuarioIdWithImages(usuarioId);
            
            if (respuestaEntidades.isExito()) {
                List<JuegoResponseDTO> juegosDTO = respuestaEntidades.getCuerpo()
                    .stream()
                    .map(JuegoResponseDTO::fromEntity)
                    .collect(Collectors.toList());
                    
                return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                        respuestaEntidades.getTipo(),
                        respuestaEntidades.getRespuesta(),
                        respuestaEntidades.getMensaje(),
                        juegosDTO
                    )
                );
            } else if (respuestaEntidades.getRespuesta() == RespuestaGlobal.RESP_ADV) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    new RespuestaGeneral<>(
                        respuestaEntidades.getTipo(),
                        respuestaEntidades.getRespuesta(),
                        respuestaEntidades.getMensaje(),
                        List.of()
                    )
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                        respuestaEntidades.getTipo(),
                        respuestaEntidades.getRespuesta(),
                        respuestaEntidades.getMensaje(),
                        null
                    )
                );
            }
        } catch (Exception e) {
            logger.error("Error al buscar juegos para usuario {}: {}", usuarioId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al buscar juegos: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Obtiene la imagen de un juego específico
     * @param id ID del juego
     * @return Imagen del juego en formato de bytes
     */
    @GetMapping("/juego/{id}/imagen")
    public ResponseEntity<?> getImagenJuego(@PathVariable Long id) {
        logger.info("Solicitando imagen para juego con ID: {}", id);
        
        
        try {
            RespuestaGeneral<Juego> respuesta = juegoService.findById(id);
            
            if (respuesta.isExito() && respuesta.getCuerpo() != null) {
                Juego juego = respuesta.getCuerpo();
                Documento imagen = juego.getImagen();
                
                if (imagen != null && imagen.getImagen() != null) {
                    byte[] imageBytes = imagen.getImagen();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
                } else {
                    logger.warn("No se encontró imagen para el juego con ID: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "No hay imagen disponible para este juego",
                            null
                        )
                    );
                }
            } else {
                logger.warn("Juego con ID {} no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
            }
        } catch (Exception e) {
            logger.error("Error al obtener imagen para juego {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al obtener imagen: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Crea un nuevo juego a partir de un objeto Juego
     * @param juego Datos completos del juego
     * @return Juego guardado
     */
    @PostMapping
    public ResponseEntity<RespuestaGeneral<JuegoResponseDTO>> crearJuego(@RequestBody JuegoDTO juegoDTO) {
        logger.info("Guardando juego: {}", juegoDTO.getTitulo());
        
        try {
            // Convertir DTO a entidad
            Juego juego = juegoDTO.toEntity();
            
            // Asignar usuario actual
            juego.setUsuario(usuarioService.getUsuarioActual());
            
            // Guardar juego
            RespuestaGeneral<Juego> respuesta = juegoService.save(juego);
            
            if (respuesta.isExito()) {
                // Convertir a DTO de respuesta (salida)
                JuegoResponseDTO juegoResponse = JuegoResponseDTO.fromEntity(respuesta.getCuerpo());
                
                logger.info("Juego guardado correctamente con ID: {} y título: {}", 
                            juegoResponse.getId(), juegoResponse.getTitulo());
                            
                return ResponseEntity.status(HttpStatus.CREATED).body(
                    new RespuestaGeneral<>(
                        respuesta.getTipo(),
                        respuesta.getRespuesta(),
                        respuesta.getMensaje(),
                        juegoResponse
                    )
                );
            } else {
                // Mismo código de error
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
            // Mismo código de error
            logger.error("Error al guardar juego: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al guardar el juego: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Elimina un juego por su ID
     * @param id ID del juego a eliminar
     * @return Respuesta del resultado de la operación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<String>> deleteJuego(@PathVariable Long id) {
        logger.info("Eliminando juego con ID: {}", id);
        
        
        RespuestaGeneral<String> respuesta = juegoService.deleteById(id);
        
        if (respuesta.isExito()) {
            logger.info("Juego con ID {} eliminado correctamente", id);
            return ResponseEntity.ok(respuesta);
        } else {
            logger.warn("Error al eliminar juego: {}", respuesta.getMensaje());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }
}