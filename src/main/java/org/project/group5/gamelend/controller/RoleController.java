package org.project.group5.gamelend.controller;

import java.util.List;

import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.mapper.RoleMapper;
import org.project.group5.gamelend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para gestionar operaciones relacionadas con roles de usuario
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden gestionar roles
public class RoleController {
    
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    
    /**
     * Obtiene todos los roles disponibles
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Solicitando lista de roles");
        
        List<Role> roles = roleService.getAllRoles();
        
        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(roleMapper.toDtoList(roles));
    }
    
    /**
     * Crea un nuevo rol
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestParam String name) {
        log.info("Creando nuevo rol: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }
        
        // Asegurar que el nombre tenga el prefijo ROLE_ si no lo tiene
        String roleName = name.startsWith("ROLE_") ? name : "ROLE_" + name;
        
        Role createdRole = roleService.createRole(roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body(roleMapper.toDto(createdRole));
    }
    
    /**
     * Busca un rol por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Buscando rol con ID: {}", id);
        
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(roleMapper.toDto(role));
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
     * Elimina un rol de un usuario
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        log.info("Eliminando rol {} de usuario {}", roleId, userId);
        
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }
}
