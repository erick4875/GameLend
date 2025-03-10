package org.project.group5.gamelend.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.project.group5.gamelend.dto.UsuarioDTO;
import org.project.group5.gamelend.dto.UsuarioResponseDTO;
import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.exception.UsuarioNoEncontradoException;
import org.project.group5.gamelend.service.UsuarioService;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Crea un nuevo usuario
     * 
     * @param usuarioDTO Datos del usuario a crear
     * @return Usuario creado
     */
    @PostMapping
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> createUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        logger.info("Creando nuevo usuario");

        if (usuarioDTO == null) {
            logger.warn("Datos de usuario nulos");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Los datos del usuario no pueden ser nulos",
                            null));
        }

        try {
            // Verificar si ya existe un usuario con el mismo nombrePublico
            if (usuarioService.existsByNombrePublico(usuarioDTO.getNombrePublico())) {
                logger.warn("Ya existe un usuario con el nombre público: {}", usuarioDTO.getNombrePublico());
                return ResponseEntity.badRequest().body(
                        new RespuestaGeneral<>(
                                RespuestaGlobal.TIPO_RESULTADO,
                                RespuestaGlobal.RESP_ERROR,
                                "Ya existe un usuario con ese nombre público",
                                null));
            }

            // Verificar si ya existe un usuario con el mismo email
            if (usuarioService.existsByEmail(usuarioDTO.getEmail())) {
                logger.warn("Ya existe un usuario con el email: {}", usuarioDTO.getEmail());
                return ResponseEntity.badRequest().body(
                        new RespuestaGeneral<>(
                                RespuestaGlobal.TIPO_RESULTADO,
                                RespuestaGlobal.RESP_ERROR,
                                "Ya existe un usuario con ese correo electrónico",
                                null));
            }

            // Si llegamos aquí, podemos proceder con la creación del usuario
            // Convertir DTO a entidad
            Usuario usuario = usuarioDTO.toEntity();

            // Establecer fecha de registro
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            usuario.setFechaRegistro(LocalDateTime.now());

            // Guardar usuario
            RespuestaGeneral<Usuario> respuesta = usuarioService.saveUsuario(usuario);

            if (respuesta.isExito()) {
                // Convertir entidad guardada a DTO
                UsuarioResponseDTO responseDTO = UsuarioResponseDTO.fromEntity(respuesta.getCuerpo());

                logger.info("Usuario creado correctamente con ID: {}", responseDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                responseDTO));
            } else {
                logger.warn("Error al crear usuario: {}", respuesta.getMensaje());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al crear el usuario: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Obtiene todos los usuarios
     * 
     * @return Lista de usuarios
     */
    @GetMapping
    @Transactional(readOnly = true) // Añadir esta anotación
    public ResponseEntity<RespuestaGeneral<List<UsuarioResponseDTO>>> getAllUsuarios() {
        logger.info("Solicitando lista de usuarios");

        try {
            RespuestaGeneral<List<UsuarioResponseDTO>> respuesta = usuarioService.getAllUsuariosDTO();

            if (respuesta.isExito()) {
                // Convertir lista de entidades a DTOs
                List<UsuarioResponseDTO> usuariosDTO = respuesta.getCuerpo();

                return ResponseEntity.ok(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                usuariosDTO));
            } else if (respuesta.getRespuesta() == RespuestaGlobal.RESP_ADV) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                List.of() // Lista vacía
                        ));
            } else {
                List<UsuarioResponseDTO> usuariosDTO = respuesta.getCuerpo();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                usuariosDTO));
            }
        } catch (Exception e) {
            logger.error("Error al obtener usuarios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al obtener usuarios: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Obtiene un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado o mensaje de error
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> getUsuarioById(@PathVariable Long id) {
        logger.info("Buscando usuario con ID: {}", id);

        if (id == null) {
            logger.warn("ID de usuario nulo");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "ID de usuario no puede ser nulo",
                            null));
        }

        try {
            RespuestaGeneral<Usuario> respuesta = usuarioService.getUsuarioById(id);

            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntity(respuesta.getCuerpo());

                return ResponseEntity.ok(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                usuarioDTO));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al buscar el usuario: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Obtiene un usuario por su email
     * 
     * @param email Email del usuario
     * @return Usuario encontrado o mensaje de error
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> getUsuarioByEmail(@PathVariable String email) {
        logger.info("Buscando usuario con email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.warn("Email nulo o vacío");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "El email no puede ser nulo o vacío",
                            null));
        }

        try {
            RespuestaGeneral<Usuario> respuesta = usuarioService.getUsuarioByEmail(email);

            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntity(respuesta.getCuerpo());

                return ResponseEntity.ok(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                usuarioDTO));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuario por email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al buscar el usuario: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Elimina un usuario por su ID
     * 
     * @param id ID del usuario a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<String>> deleteUsuario(@PathVariable Long id) {
        logger.info("Eliminando usuario con ID: {}", id);

        if (id == null) {
            logger.warn("ID de usuario nulo");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "ID de usuario no puede ser nulo",
                            null));
        }

        try {
            RespuestaGeneral<String> respuesta = usuarioService.deleteUsuario(id);

            if (respuesta.isExito()) {
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al eliminar el usuario: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Actualiza un usuario existente
     * 
     * @param id         ID del usuario a actualizar
     * @param usuarioDTO Nuevos datos del usuario
     * @return Usuario actualizado o mensaje de error
     */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> updateUsuario(@PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {
        logger.info("Actualizando usuario con ID: {}", id);

        if (id == null || usuarioDTO == null) {
            logger.warn("ID o datos de usuario nulos");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "ID y datos del usuario no pueden ser nulos",
                            null));
        }

        try {
            // Convertir DTO a entidad
            Usuario usuario = usuarioDTO.toEntity();
            usuario.setId(id); // Asegurar que se actualiza el usuario correcto

            RespuestaGeneral<Usuario> respuesta = usuarioService.updateUsuario(id, usuario);

            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                UsuarioResponseDTO responseDTO = UsuarioResponseDTO.fromEntity(respuesta.getCuerpo());

                return ResponseEntity.ok(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                responseDTO));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al actualizar el usuario: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Obtiene el perfil básico de un usuario por su email
     * 
     * @param email Email del usuario
     * @return Datos del perfil del usuario
     */
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> obtenerPerfilUsuario(@RequestParam String email) {
        logger.info("Solicitando perfil para usuario con email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.warn("Email nulo o vacío");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "El email no puede ser nulo o vacío",
                            null));
        }

        try {
            Usuario usuario = usuarioService.getUsuarioByEmail(email).getCuerpo();
            UsuarioResponseDTO usuarioResponseDTO = UsuarioResponseDTO.fromEntity(usuario);

            return ResponseEntity.ok(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_OK,
                            RespuestaGlobal.OPER_CORRECTA,
                            usuarioResponseDTO));
        } catch (UsuarioNoEncontradoException e) {
            logger.warn("Usuario con email {} no encontrado", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            e.getMessage(),
                            null));
        } catch (Exception e) {
            logger.error("Error al obtener perfil para usuario {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al obtener perfil: " + e.getMessage(),
                            null));
        }
    }

    /**
     * Endpoint de prueba
     * 
     * @return Mensaje simple para verificar que el controlador funciona
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("UsuarioController está funcionando correctamente");
    }

    /**
     * Obtiene un usuario completo por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado o mensaje de error
     */
    @GetMapping("/{id}/completo")
    @Transactional(readOnly = true)
    public ResponseEntity<RespuestaGeneral<UsuarioResponseDTO>> getUsuarioCompleto(@PathVariable Long id) {
        logger.info("Buscando usuario completo con ID: {}", id);

        if (id == null) {
            logger.warn("ID de usuario nulo");
            return ResponseEntity.badRequest().body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "ID de usuario no puede ser nulo",
                            null));
        }

        try {
            RespuestaGeneral<Usuario> respuesta = usuarioService.getUsuarioCompletoById(id);

            if (respuesta.isExito()) {
                // Convertir entidad a DTO
                UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntityCompleto(respuesta.getCuerpo());

                return ResponseEntity.ok(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                usuarioDTO));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new RespuestaGeneral<>(
                                respuesta.getTipo(),
                                respuesta.getRespuesta(),
                                respuesta.getMensaje(),
                                null));
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuario completo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RespuestaGeneral<>(
                            RespuestaGlobal.TIPO_RESULTADO,
                            RespuestaGlobal.RESP_ERROR,
                            "Error al buscar el usuario completo: " + e.getMessage(),
                            null));
        }
    }
}
