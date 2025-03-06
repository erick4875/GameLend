package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.entity.UsuarioRol;
import org.project.group5.gamelend.service.UsuarioRolService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para gestionar operaciones relacionadas con roles de usuario
 */
@RestController
@RequestMapping("/api/roles")
public class RolController {
    private static final Logger logger = LoggerFactory.getLogger(RolController.class);
    
    private final UsuarioRolService usuarioRolService;
    
    public RolController(UsuarioRolService usuarioRolService) {
        this.usuarioRolService = usuarioRolService;
    }
    
    /**
     * Obtiene todos los roles disponibles
     */
    @GetMapping
    public ResponseEntity<RespuestaGeneral<List<UsuarioRol>>> getAllRoles() {
        logger.info("Solicitando lista de roles");
        
        try {
            RespuestaGeneral<List<UsuarioRol>> respuesta = usuarioRolService.obtenerTodosLosRoles();
            
            if (respuesta.isExito()) {
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
            }
        } catch (Exception e) {
            logger.error("Error al obtener lista de roles: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "Error al obtener la lista de roles: " + e.getMessage(),
                    null
                )
            );
        }
    }
    
    /**
     * Crea un nuevo rol
     */
    @PostMapping
    public ResponseEntity<RespuestaGeneral<UsuarioRol>> createRol(@RequestParam String nombre) {
        logger.info("Creando nuevo rol: {}", nombre);
        
        if (nombre == null || nombre.trim().isEmpty()) {
            logger.warn("Intento de crear rol con nombre nulo o vacío");
            return ResponseEntity.badRequest().body(
                new RespuestaGeneral<>(
                    RespuestaGlobal.TIPO_RESULTADO,
                    RespuestaGlobal.RESP_ERROR,
                    "El nombre del rol no puede estar vacío",
                    null
                )
            );
        }
        
        RespuestaGeneral<UsuarioRol> respuesta = usuarioRolService.crearRol(nombre);
        
        if (respuesta.isExito()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
    
    /**
     * Asigna un rol a un usuario
     */
    @PostMapping("/asignar")
    public ResponseEntity<RespuestaGeneral<String>> asignarRol(
            @RequestParam Long usuarioId, 
            @RequestParam Long rolId) {
        logger.info("Solicitud para asignar rol {} al usuario {}", rolId, usuarioId);
        RespuestaGeneral<String> respuesta = usuarioRolService.asignarRolAUsuario(usuarioId, rolId);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
    
    /**
     * Quita un rol a un usuario
     */
    @DeleteMapping("/quitar")
    public ResponseEntity<RespuestaGeneral<String>> quitarRol(
            @RequestParam Long usuarioId, 
            @RequestParam Long rolId) {
        logger.info("Solicitud para quitar rol {} al usuario {}", rolId, usuarioId);
        RespuestaGeneral<String> respuesta = usuarioRolService.quitarRolAUsuario(usuarioId, rolId);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
    
    /**
     * Obtiene los roles de un usuario específico
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<RespuestaGeneral<List<UsuarioRol>>> getRolesPorUsuario(
            @PathVariable Long usuarioId) {
        logger.info("Obteniendo roles para usuario con ID: {}", usuarioId);
        RespuestaGeneral<List<UsuarioRol>> respuesta = usuarioRolService.obtenerRolesPorUsuario(usuarioId);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }
    
    /**
     * Obtiene un rol por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaGeneral<UsuarioRol>> getRolById(@PathVariable Long id) {
        logger.info("Buscando rol con ID: {}", id);
        RespuestaGeneral<UsuarioRol> respuesta = usuarioRolService.obtenerRolPorId(id);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }
    
    /**
     * Busca un rol por su nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<RespuestaGeneral<UsuarioRol>> getRolByName(@RequestParam String nombre) {
        logger.info("Buscando rol con nombre: {}", nombre);
        RespuestaGeneral<UsuarioRol> respuesta = usuarioRolService.obtenerRolPorNombre(nombre);
        
        if (respuesta.isExito()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
    }
}
