package org.project.group5.gamelend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.dto.UsuarioDTO;
import org.project.group5.gamelend.dto.UsuarioResponseDTO;
import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.exception.UnauthorizedException;
import org.project.group5.gamelend.exception.UsuarioNoEncontradoException;
import org.project.group5.gamelend.repository.UsuarioRepository;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de usuarios
 */
@Service
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea o actualiza un usuario
     * @param usuario El usuario a guardar
     * @return Respuesta con el usuario guardado o mensaje de error
     */
    public RespuestaGeneral<Usuario> saveUsuario(Usuario usuario) {
        try {
            // Verificar si ya existe un usuario con el mismo nombrePublico o email
            if (existsByNombrePublico(usuario.getNombrePublico())) {
                logger.warn("Ya existe un usuario con el nombre público: {}", usuario.getNombrePublico());
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Ya existe un usuario con ese nombre público",
                        null);
            }

            if (existsByEmail(usuario.getEmail())) {
                logger.warn("Ya existe un usuario con el email: {}", usuario.getEmail());
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Ya existe un usuario con ese correo electrónico",
                        null);
            }

            // Encriptar la contraseña si no está encriptada ya
            if (!usuario.getPassword().startsWith("$2a$")) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }

            // Guardar el usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            logger.info("Usuario guardado correctamente con ID: {}", usuarioGuardado.getId());

            return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    "Usuario guardado correctamente",
                    usuarioGuardado);
        } catch (DataIntegrityViolationException e) {
            // Detectar tipo específico de violación de unicidad
            String mensaje = e.getMessage() != null ? e.getMessage() : "";
            
            if (mensaje.contains("nombre_publico")) {
                logger.error("Error al guardar usuario: Nombre público duplicado", e);
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Ya existe un usuario con ese nombre público",
                        null);
            } else if (mensaje.contains("email")) {
                logger.error("Error al guardar usuario: Email duplicado", e);
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Ya existe un usuario con ese correo electrónico",
                        null);
            } else {
                logger.error("Error al guardar usuario: Violación de integridad de datos", e);
                return new RespuestaGeneral<>(
                        RespuestaGlobal.TIPO_RESULTADO,
                        RespuestaGlobal.RESP_ERROR,
                        "Error al guardar el usuario: Violación de integridad de datos",
                        null);
            }
        } catch (Exception e) {
            logger.error("Error al guardar usuario", e);
            return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al guardar el usuario: " + e.getMessage(),
                    null);
        }
    }

    /**
     * Obtiene todos los usuarios
     * @return Respuesta con la lista de usuarios o mensaje de advertencia
     */
    public RespuestaGeneral<List<Usuario>> getAllUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            if (usuarios.isEmpty()) {
                logger.info("No se encontraron usuarios");
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "No se encontraron usuarios",
                    null
                );
            }
            
            logger.debug("Se encontraron {} usuarios", usuarios.size());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                usuarios
            );
        } catch (Exception e) {
            logger.error("Error al obtener usuarios: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener usuarios: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Respuesta con el usuario o mensaje de error
     */
    public RespuestaGeneral<Usuario> getUsuarioById(Long id) {
        if (id == null) {
            logger.warn("ID de usuario nulo");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de usuario no puede ser nulo",
                null
            );
        }

        try {
            Optional<Usuario> usuario = usuarioRepository.findById(id);
            if (usuario.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    usuario.get()
                );
            }
            
            logger.info("Usuario con ID {} no encontrado", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Usuario no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar usuario con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar el usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un usuario por su email
     * @param email Email del usuario
     * @return Respuesta con el usuario o mensaje de error
     */
    public RespuestaGeneral<Usuario> getUsuarioByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Email de usuario nulo o vacío");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Email de usuario no puede ser nulo o vacío",
                null
            );
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    usuarioOpt.get()
                );
            }
            
            logger.info("Usuario con email {} no encontrado", email);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Usuario no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar usuario con email {}: {}", email, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar el usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    public RespuestaGeneral<String> deleteUsuario(Long id) {
        if (id == null) {
            logger.warn("ID de usuario nulo en intento de eliminación");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de usuario no puede ser nulo",
                null
            );
        }

        try {
            if (usuarioRepository.existsById(id)) {
                usuarioRepository.deleteById(id);
                logger.info("Usuario con ID {} eliminado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    "Usuario eliminado correctamente",
                    null
                );
            }
            
            logger.info("Intento de eliminar un usuario inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Usuario no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al eliminar el usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuario Nuevos datos del usuario
     * @return Respuesta con el usuario actualizado o mensaje de error
     */
    public RespuestaGeneral<Usuario> updateUsuario(Long id, Usuario usuario) {
        if (id == null || usuario == null) {
            logger.warn("ID o datos de usuario nulos en actualización");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID y datos del usuario no pueden ser nulos",
                null
            );
        }

        try {
            if (usuarioRepository.existsById(id)) {
                usuario.setId(id);
                Usuario updated = usuarioRepository.save(usuario);
                logger.info("Usuario con ID {} actualizado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    updated
                );
            }
            
            logger.info("Intento de actualizar un usuario inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Usuario no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al actualizar el usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un DTO de usuario por su email
     * @param email Email del usuario
     * @return DTO con información parcial del usuario
     * @throws UsuarioNoEncontradoException si el usuario no existe
     */
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario con email {} no encontrado", email);
                throw new UsuarioNoEncontradoException("Usuario con email: " + email + " no encontrado");
            }
            
            logger.debug("Usuario con email {} encontrado correctamente", email);
            Usuario usuario = usuarioOpt.get();
            return new UsuarioDTO(usuario.getNombrePublico(), usuario.getEmail(), usuario.getFechaRegistro().toString());
        } catch (UsuarioNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener usuario por email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al obtener el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un DTO de usuario con sus juegos por nombre público
     * @param nombrePublico Nombre público del usuario
     * @return DTO con información del usuario y sus juegos
     * @throws UsuarioNoEncontradoException si el usuario no existe
     */
    public UsuarioDTO obtenerJuegosPorNombrePublico(String nombrePublico) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByNombrePublico(nombrePublico);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario con nombre público {} no encontrado", nombrePublico);
                throw new UsuarioNoEncontradoException("Usuario con nombrePublico: " + nombrePublico + " no existe");
            }
            
            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario con nombre público {} y {} juegos encontrado", nombrePublico, usuario.getJuegos().size());
            return new UsuarioDTO(usuario.getNombrePublico(), usuario.getJuegos());
        } catch (UsuarioNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener juegos para usuario {}: {}", nombrePublico, e.getMessage(), e);
            throw new RuntimeException("Error al obtener juegos del usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario autenticado
     * @throws UnauthorizedException si no hay usuario autenticado
     */
    public Usuario getUsuarioActual() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }
        
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado: " + email);
        }
        return usuarioOpt.get();
    }

    public RespuestaGeneral<List<UsuarioResponseDTO>> getAllUsuariosDTO() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<UsuarioResponseDTO> dtos = usuarios.stream()
                .map(UsuarioResponseDTO::fromEntity)  // Usa fromEntity básico
                .collect(Collectors.toList());
            
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                dtos
            );
        } catch (Exception e) {
            logger.error("Error al obtener todos los usuarios DTO: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener usuarios: " + e.getMessage(),
                null
            );
        }
    }

    @Transactional(readOnly = true)
    public RespuestaGeneral<UsuarioResponseDTO> getUsuarioCompletoDTO(Long id) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(id);
            // Aquí las colecciones se cargarán correctamente dentro de la transacción
            
            if (usuario.isPresent()) {
                UsuarioResponseDTO dto = UsuarioResponseDTO.fromEntityCompleto(usuario.get());
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    dto
                );
            }
            
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Usuario no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al obtener usuario completo DTO con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener usuario: " + e.getMessage(),
                null
            );
        }
    }

    public RespuestaGeneral<UsuarioResponseDTO> getUsuarioByIdDTO(Long id) {
        RespuestaGeneral<Usuario> respuestaEntidad = getUsuarioById(id);
        if (!respuestaEntidad.isExito() || respuestaEntidad.getCuerpo() == null) {
            return new RespuestaGeneral<>(
                respuestaEntidad.getTipo(),
                respuestaEntidad.getRespuesta(),
                respuestaEntidad.getMensaje(),
                null
            );
        }
        return new RespuestaGeneral<>(
            respuestaEntidad.getTipo(),
            respuestaEntidad.getRespuesta(),
            respuestaEntidad.getMensaje(),
            UsuarioResponseDTO.fromEntity(respuestaEntidad.getCuerpo())
        );
    }

    /**
     * Obtiene un usuario por su email y lo convierte a DTO
     * @param email Email del usuario
     * @return Respuesta con el DTO del usuario
     */
    public RespuestaGeneral<UsuarioResponseDTO> getUsuarioByEmailDTO(String email) {
        try {
            RespuestaGeneral<Usuario> respuestaEntidad = getUsuarioByEmail(email);
            
            if (!respuestaEntidad.isExito() || respuestaEntidad.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuestaEntidad.getTipo(),
                    respuestaEntidad.getRespuesta(),
                    respuestaEntidad.getMensaje(),
                    null
                );
            }
            
            UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntity(respuestaEntidad.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuestaEntidad.getTipo(),
                respuestaEntidad.getRespuesta(),
                respuestaEntidad.getMensaje(),
                usuarioDTO
            );
        } catch (Exception e) {
            logger.error("Error al obtener DTO de usuario con email {}: {}", email, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Actualiza un usuario y devuelve el DTO actualizado
     * @param id ID del usuario a actualizar
     * @param usuario Nuevos datos del usuario
     * @return Respuesta con el DTO del usuario actualizado
     */
    public RespuestaGeneral<UsuarioResponseDTO> updateUsuarioDTO(Long id, Usuario usuario) {
        try {
            RespuestaGeneral<Usuario> respuestaEntidad = updateUsuario(id, usuario);
            
            if (!respuestaEntidad.isExito() || respuestaEntidad.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuestaEntidad.getTipo(),
                    respuestaEntidad.getRespuesta(),
                    respuestaEntidad.getMensaje(),
                    null
                );
            }
            
            UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntity(respuestaEntidad.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuestaEntidad.getTipo(),
                respuestaEntidad.getRespuesta(),
                respuestaEntidad.getMensaje(),
                usuarioDTO
            );
        } catch (Exception e) {
            logger.error("Error al actualizar y convertir a DTO usuario con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al actualizar usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Guarda un usuario y devuelve el DTO
     * @param usuario Usuario a guardar
     * @return Respuesta con el DTO del usuario guardado
     */
    public RespuestaGeneral<UsuarioResponseDTO> saveUsuarioDTO(Usuario usuario) {
        try {
            RespuestaGeneral<Usuario> respuestaEntidad = saveUsuario(usuario);
            
            if (!respuestaEntidad.isExito() || respuestaEntidad.getCuerpo() == null) {
                return new RespuestaGeneral<>(
                    respuestaEntidad.getTipo(),
                    respuestaEntidad.getRespuesta(),
                    respuestaEntidad.getMensaje(),
                    null
                );
            }
            
            UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromEntity(respuestaEntidad.getCuerpo());
            
            return new RespuestaGeneral<>(
                respuestaEntidad.getTipo(),
                respuestaEntidad.getRespuesta(),
                respuestaEntidad.getMensaje(),
                usuarioDTO
            );
        } catch (Exception e) {
            logger.error("Error al guardar y convertir a DTO usuario: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al guardar usuario: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene el usuario actual y lo convierte a DTO
     * @return DTO del usuario autenticado
     * @throws UnauthorizedException si no hay usuario autenticado
     */
    public UsuarioResponseDTO getUsuarioActualDTO() throws UnauthorizedException {
        Usuario usuario = getUsuarioActual();
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    /**
     * Obtiene un DTO de usuario por su email
     * @param email Email del usuario
     * @return DTO con información del usuario
     * @throws UsuarioNoEncontradoException si el usuario no existe
     */
    public UsuarioResponseDTO obtenerUsuarioPorEmailResponseDTO(String email) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario con email {} no encontrado", email);
                throw new UsuarioNoEncontradoException("Usuario con email: " + email + " no encontrado");
            }
            
            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario con email {} encontrado correctamente", email);
            return UsuarioResponseDTO.fromEntity(usuario);
        } catch (UsuarioNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener usuario por email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al obtener el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un usuario completo con todos sus datos relacionados por su ID
     * 
     * @param id ID del usuario
     * @return RespuestaGeneral con el usuario o mensaje de error
     */
    @Transactional(readOnly = true)
    public RespuestaGeneral<Usuario> getUsuarioCompletoById(Long id) {
        try {
            // Aquí puedes usar un método personalizado del repositorio que cargue las relaciones
            Optional<Usuario> usuarioOpt = usuarioRepository.findByIdWithRelaciones(id);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Inicializar colecciones lazy si no se han cargado con la consulta
                Hibernate.initialize(usuario.getJuegos());
                // Puedes inicializar más colecciones si las hay
                
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    usuario);
            } else {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Usuario no encontrado con ID: " + id,
                    null);
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuario completo con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar usuario completo: " + e.getMessage(),
                null);
        }
    }

    /**
     * Verifica si existe un usuario con el nombre público dado
     * 
     * @param nombrePublico Nombre público a verificar
     * @return true si existe un usuario con ese nombre público, false en caso contrario
     */
    public boolean existsByNombrePublico(String nombrePublico) {
        if (nombrePublico == null || nombrePublico.trim().isEmpty()) {
            return false;
        }
        
        try {
            return usuarioRepository.existsByNombrePublico(nombrePublico);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de usuario por nombre público: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica si existe un usuario con el email dado
     * 
     * @param email Email a verificar
     * @return true si existe un usuario con ese email, false en caso contrario
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            return usuarioRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de usuario por email: {}", e.getMessage(), e);
            return false;
        }
    }
}