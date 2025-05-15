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
 * Gestiona roles y su asignación.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Solo ADMINS.
public class RoleController {

    private final RoleService roleService;

    /**
     * Obtiene todos los roles.
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Solicitando lista de roles");
        List<RoleDTO> roles = roleService.getAllRoles();

        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roles);
    }

    /**
     * Crea un nuevo rol.
     * Body: {"name": "NUEVO_ROL"}
     * El prefijo "ROLE_" se maneja en el servicio.
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        log.info("Creando nuevo rol con datos: {}", roleDTO);

        if (roleDTO == null || roleDTO.name() == null || roleDTO.name().trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol en el DTO no puede estar vacío");
        }

        RoleDTO createdRoleDTO = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoleDTO);
    }

    /**
     * Busca un rol por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Buscando rol con ID: {}", id);
        RoleDTO roleDTO = roleService.getRoleById(id);
        return ResponseEntity.ok(roleDTO);
    }

    /**
     * Actualiza un rol.
     * Body: {"name": "ROL_ACTUALIZADO"}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        log.info("Actualizando rol con ID: {} con datos: {}", id, roleDTO);

        if (roleDTO == null || roleDTO.name() == null || roleDTO.name().trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol en el DTO no puede estar vacío para la actualización");
        }

        RoleDTO updatedRoleDTO = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRoleDTO);
    }

    /**
     * Elimina un rol por ID.
     * DELETE /api/roles/{id}
     * 
     * @param id ID del rol a eliminar.
     * @return Estado 204 (NO CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Eliminando rol con ID: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna un rol a un usuario.
     * POST /api/roles/assign?userId={userId}&roleId={roleId}
     * 
     * @param userId ID del usuario.
     * @param roleId ID del rol a asignar.
     * @return Estado 200 (OK).
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
     * Elimina un rol de un usuario.
     * DELETE /api/roles/remove?userId={userId}&roleId={roleId}
     * 
     * @param userId ID del usuario.
     * @param roleId ID del rol a eliminar.
     * @return Estado 200 (OK).
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        log.info("Eliminando rol {} de usuario {}", roleId, userId);
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene los roles de un usuario.
     * GET /api/roles/user/{userId}
     * 
     * @param userId ID del usuario.
     * @return Lista de roles del usuario y estado 200 (OK) o 204 (NO CONTENT).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoleDTO>> getRolesByUser(@PathVariable Long userId) {
        log.info("Solicitando roles para el usuario con ID: {}", userId);
        List<RoleDTO> userRoles = roleService.getRolesByUser(userId);
        if (userRoles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userRoles);
    }
}
