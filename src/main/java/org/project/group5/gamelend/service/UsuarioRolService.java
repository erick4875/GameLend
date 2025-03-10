package org.project.group5.gamelend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.entity.UsuarioRol;
import org.project.group5.gamelend.repository.UsuarioRepository;
import org.project.group5.gamelend.repository.UsuarioRolRepository;
import org.project.group5.gamelend.util.RespuestaGeneral;
import org.project.group5.gamelend.util.RespuestaGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Servicio para la gestión de roles de usuario
 */
@Service
public class UsuarioRolService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioRolService.class);
    
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository; // Añadir esta línea
    
    // Modificar el constructor para recibir el nuevo repositorio
    public UsuarioRolService(UsuarioRolRepository usuarioRolRepository, 
                           UsuarioRepository usuarioRepository) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un nuevo rol de usuario
     * @param name Nombre del rol
     * @return Respuesta con el rol creado o mensaje de error
     */
    public RespuestaGeneral<UsuarioRol> crearRol(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Intento de crear rol con nombre nulo o vacío");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "El nombre del rol no puede ser nulo o vacío",
                null
            );
        }

        try {
            // Verificar si ya existe un rol con ese nombre
            if (usuarioRolRepository.existsByName(name)) {
                logger.info("Intento de crear rol con nombre ya existente: {}", name);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Ya existe un rol con ese nombre",
                    null
                );
            }

            UsuarioRol rol = new UsuarioRol(name);
            UsuarioRol rolGuardado = usuarioRolRepository.save(rol);
            logger.info("Rol creado correctamente: {}", name);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                rolGuardado
            );
        } catch (Exception e) {
            logger.error("Error al crear rol {}: {}", name, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al crear el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene todos los roles de usuario
     * @return Respuesta con la lista de roles o mensaje de advertencia
     */
    public RespuestaGeneral<List<UsuarioRol>> obtenerTodosLosRoles() {
        try {
            List<UsuarioRol> roles = usuarioRolRepository.findAll();
            if (roles.isEmpty()) {
                logger.info("No se encontraron roles");
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "No se encontraron roles",
                    null
                );
            }

            logger.debug("Se encontraron {} roles", roles.size());
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                roles
            );
        } catch (Exception e) {
            logger.error("Error al obtener todos los roles: {}", e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener los roles: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene un rol por su ID
     * @param id ID del rol a buscar
     * @return Respuesta con el rol encontrado o mensaje de error
     */
    public RespuestaGeneral<UsuarioRol> obtenerRolPorId(Long id) {
        if (id == null) {
            logger.warn("ID de rol nulo");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID del rol no puede ser nulo",
                null
            );
        }

        try {
            Optional<UsuarioRol> rol = usuarioRolRepository.findById(id);
            if (rol.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    rol.get()
                );
            }
            
            logger.info("Rol con ID {} no encontrado", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Rol no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar rol con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Elimina un rol por su ID
     * @param id ID del rol a eliminar
     * @return Respuesta indicando el resultado de la operación
     */
    public RespuestaGeneral<String> eliminarRol(Long id) {
        if (id == null) {
            logger.warn("ID de rol nulo en intento de eliminación");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID del rol no puede ser nulo",
                null
            );
        }

        try {
            if (usuarioRolRepository.existsById(id)) {
                usuarioRolRepository.deleteById(id);
                logger.info("Rol con ID {} eliminado correctamente", id);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    "Rol eliminado correctamente",
                    null
                );
            }
            
            logger.info("Intento de eliminar un rol inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Rol no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al eliminar rol con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al eliminar el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Actualiza un rol existente
     * @param id ID del rol a actualizar
     * @param newName Nuevo nombre para el rol
     * @return Respuesta con el rol actualizado o mensaje de error
     */
    public RespuestaGeneral<UsuarioRol> actualizarRol(Long id, String newName) {
        if (id == null || newName == null || newName.trim().isEmpty()) {
            logger.warn("ID de rol nulo o nombre nulo/vacío en actualización");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID del rol y nuevo nombre no pueden ser nulos o vacíos",
                null
            );
        }

        try {
            Optional<UsuarioRol> rolOptional = usuarioRolRepository.findById(id);
            if (rolOptional.isPresent()) {
                UsuarioRol rol = rolOptional.get();
                rol.setName(newName);
                UsuarioRol rolActualizado = usuarioRolRepository.save(rol);
                logger.info("Rol con ID {} actualizado correctamente a: {}", id, newName);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    rolActualizado
                );
            }
            
            logger.info("Intento de actualizar un rol inexistente con ID: {}", id);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Rol no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al actualizar rol con ID {}: {}", id, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al actualizar el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Asigna un rol a un usuario
     * @param usuarioId ID del usuario
     * @param rolId ID del rol a asignar
     * @return Respuesta indicando el resultado de la operación
     */
    @Transactional
    public RespuestaGeneral<String> asignarRolAUsuario(Long usuarioId, Long rolId) {
        if (usuarioId == null || rolId == null) {
            logger.warn("ID de usuario o rol nulo en intento de asignación");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de usuario y rol no pueden ser nulos",
                null
            );
        }

        try {
            // Buscar usuario y rol
            Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
            Optional<UsuarioRol> rolOptional = usuarioRolRepository.findById(rolId);
            
            // Verificar que ambos existan
            if (!usuarioOptional.isPresent()) {
                logger.warn("Usuario con ID {} no encontrado", usuarioId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Usuario no encontrado",
                    null
                );
            }
            
            if (!rolOptional.isPresent()) {
                logger.warn("Rol con ID {} no encontrado", rolId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Rol no encontrado",
                    null
                );
            }
            
            Usuario usuario = usuarioOptional.get();
            UsuarioRol rol = rolOptional.get();
            
            // Verificar si el usuario ya tiene ese rol
            if (usuario.getRoles().stream().anyMatch(r -> r.getId() == rolId)) {
                logger.info("El usuario {} ya tiene asignado el rol {}", usuarioId, rolId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "El usuario ya tiene asignado ese rol",
                    null
                );
            }
            
            // Asignar el rol
            usuario.agregarRol(rol);
            usuarioRepository.save(usuario);
            
            logger.info("Rol {} asignado correctamente al usuario {}", rolId, usuarioId);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                "Rol asignado correctamente",
                null
            );
        } catch (Exception e) {
            logger.error("Error al asignar rol {} al usuario {}: {}", rolId, usuarioId, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al asignar el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Quita un rol a un usuario
     * @param usuarioId ID del usuario
     * @param rolId ID del rol a quitar
     * @return Respuesta indicando el resultado de la operación
     */
    @Transactional
    public RespuestaGeneral<String> quitarRolAUsuario(Long usuarioId, Long rolId) {
        if (usuarioId == null || rolId == null) {
            logger.warn("ID de usuario o rol nulo en intento de eliminación de rol");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "ID de usuario y rol no pueden ser nulos",
                null
            );
        }

        try {
            // Buscar usuario y rol
            Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
            Optional<UsuarioRol> rolOptional = usuarioRolRepository.findById(rolId);
            
            // Verificar que ambos existan
            if (!usuarioOptional.isPresent()) {
                logger.warn("Usuario con ID {} no encontrado", usuarioId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Usuario no encontrado",
                    null
                );
            }
            
            if (!rolOptional.isPresent()) {
                logger.warn("Rol con ID {} no encontrado", rolId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Rol no encontrado",
                    null
                );
            }
            
            Usuario usuario = usuarioOptional.get();
            UsuarioRol rol = rolOptional.get();
            
            // Verificar si el usuario tiene ese rol
            if (usuario.getRoles().stream().noneMatch(r -> r.getId() == rolId)) {
                logger.info("El usuario {} no tiene asignado el rol {}", usuarioId, rolId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "El usuario no tiene asignado ese rol",
                    null
                );
            }
            
            // Quitar el rol
            usuario.quitarRol(rol);
            usuarioRepository.save(usuario);
            
            logger.info("Rol {} eliminado correctamente del usuario {}", rolId, usuarioId);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                "Rol eliminado correctamente",
                null
            );
        } catch (Exception e) {
            logger.error("Error al quitar rol {} al usuario {}: {}", rolId, usuarioId, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al quitar el rol: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Obtiene todos los roles de un usuario específico
     * @param usuarioId ID del usuario
     * @return Respuesta con la lista de roles del usuario
     */
    public RespuestaGeneral<List<UsuarioRol>> obtenerRolesPorUsuario(Long usuarioId) {
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
            Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
            
            if (!usuarioOptional.isPresent()) {
                logger.warn("Usuario con ID {} no encontrado", usuarioId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Usuario no encontrado",
                    null
                );
            }
            
            Usuario usuario = usuarioOptional.get();
            Set<UsuarioRol> roles = usuario.getRoles();
            
            if (roles.isEmpty()) {
                logger.info("El usuario {} no tiene roles asignados", usuarioId);
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ADV,
                    "El usuario no tiene roles asignados",
                    new ArrayList<>()
                );
            }
            
            logger.info("Se encontraron {} roles para el usuario {}", roles.size(), usuarioId);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_OK,
                RespuestaGlobal.OPER_CORRECTA,
                new ArrayList<>(roles)
            );
        } catch (Exception e) {
            logger.error("Error al obtener roles del usuario {}: {}", usuarioId, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al obtener los roles: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Busca un rol por su nombre
     * @param nombreRol nombre del rol
     * @return Respuesta con el rol encontrado o mensaje de error
     */
    public RespuestaGeneral<UsuarioRol> obtenerRolPorNombre(String nombreRol) {
        if (nombreRol == null || nombreRol.trim().isEmpty()) {
            logger.warn("Nombre de rol nulo o vacío");
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Nombre de rol no puede ser nulo o vacío",
                null
            );
        }

        try {
            Optional<UsuarioRol> rol = usuarioRolRepository.findByName(nombreRol);
            
            if (rol.isPresent()) {
                return new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_OK,
                    RespuestaGlobal.OPER_CORRECTA,
                    rol.get()
                );
            }
            
            logger.info("Rol con nombre '{}' no encontrado", nombreRol);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Rol no encontrado",
                null
            );
        } catch (Exception e) {
            logger.error("Error al buscar rol con nombre {}: {}", nombreRol, e.getMessage(), e);
            return new RespuestaGeneral<>(
                RespuestaGlobal.TIPO_RESULTADO,
                RespuestaGlobal.RESP_ERROR,
                "Error al buscar el rol: " + e.getMessage(),
                null
            );
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaGeneral<List<UsuarioRol>>> getAllRoles() {
        logger.info("Solicitando lista de roles");
        RespuestaGeneral<List<UsuarioRol>> respuesta = obtenerTodosLosRoles();
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }
}
