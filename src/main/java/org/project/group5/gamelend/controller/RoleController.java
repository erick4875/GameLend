package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de roles.
 * Requiere rol ADMIN para todas las operaciones.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    /**
     * Lista todos los roles
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Solicitando lista de roles");
        List<RoleDTO> roles = roleService.getAllRoles();
        return roles.isEmpty() ? 
            ResponseEntity.noContent().build() : 
            ResponseEntity.ok(roles);
    }

    /**
     * Crea un nuevo rol
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        log.info("Creando rol: {}", roleDTO);
        validateRoleDTO(roleDTO, "crear");
        RoleDTO created = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtiene un rol por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Buscando rol ID: {}", id);
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    /**
     * Actualiza un rol existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long id, 
            @RequestBody RoleDTO roleDTO) {
        log.info("Actualizando rol ID: {} con datos: {}", id, roleDTO);
        validateRoleDTO(roleDTO, "actualizar");
        return ResponseEntity.ok(roleService.updateRole(id, roleDTO));
    }

    /**
     * Elimina un rol
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Eliminando rol ID: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna un rol a un usuario
     */
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRoleToUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        log.info("Asignando rol {} a usuario {}", roleId, userId);
        roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remueve un rol de un usuario
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        log.info("Removiendo rol {} de usuario {}", roleId, userId);
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lista los roles de un usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoleDTO>> getRolesByUser(@PathVariable Long userId) {
        log.info("Listando roles del usuario ID: {}", userId);
        List<RoleDTO> roles = roleService.getRolesByUser(userId);
        return roles.isEmpty() ? 
            ResponseEntity.noContent().build() : 
            ResponseEntity.ok(roles);
    }

    /**
     * Valida el DTO de rol
     */
    private void validateRoleDTO(RoleDTO roleDTO, String operacion) {
        if (roleDTO == null || roleDTO.name() == null || roleDTO.name().trim().isEmpty()) {
            throw new BadRequestException(
                "El nombre del rol es requerido para " + operacion);
        }
    }
}
