package org.project.group5.gamelend.service;

import java.util.List;

import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de roles de usuario
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Crea un nuevo rol de usuario
     * 
     * @param name Nombre del rol
     * @return El rol creado
     * @throws BadRequestException si el nombre es nulo o vacío
     */
    public Role createRole(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Intento de crear rol con nombre nulo o vacío");
            throw new BadRequestException("El nombre del rol no puede ser nulo o vacío");
        }

        // Verificar si ya existe un rol con ese nombre
        if (roleRepository.existsByName(name)) {
            log.info("Intento de crear rol con nombre ya existente: {}", name);
            throw new BadRequestException("Ya existe un rol con ese nombre");
        }

        Role role = new Role();
        role.setName(name);
        Role savedRole = roleRepository.save(role);
        log.info("Rol creado correctamente: {}", name);
        
        return savedRole;
    }

    /**
     * Obtiene todos los roles de usuario
     * 
     * @return Lista de roles (puede estar vacía)
     */
    public List<Role> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        log.debug("Se encontraron {} roles", roles.size());
        return roles;
    }

    /**
     * Obtiene un rol por su ID
     * 
     * @param id ID del rol a buscar
     * @return El rol encontrado
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si no existe el rol
     */
    public Role getRoleById(Long id) {
        if (id == null) {
            log.warn("ID de rol nulo");
            throw new BadRequestException("ID del rol no puede ser nulo");
        }

        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Rol con ID {} no encontrado", id);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + id);
                });
    }

    /**
     * Elimina un rol por su ID
     * 
     * @param id ID del rol a eliminar
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si no existe el rol
     */
    public void deleteRole(Long id) {
        if (id == null) {
            log.warn("ID de rol nulo en intento de eliminación");
            throw new BadRequestException("ID del rol no puede ser nulo");
        }

        if (!roleRepository.existsById(id)) {
            log.info("Intento de eliminar un rol inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        
        roleRepository.deleteById(id);
        log.info("Rol con ID {} eliminado correctamente", id);
    }

    /**
     * Actualiza un rol existente
     * 
     * @param id      ID del rol a actualizar
     * @param newName Nuevo nombre para el rol
     * @return El rol actualizado
     * @throws BadRequestException si el ID o nombre son nulos
     * @throws ResourceNotFoundException si no existe el rol
     */
    public Role updateRole(Long id, String newName) {
        if (id == null || newName == null || newName.trim().isEmpty()) {
            log.warn("ID de rol nulo o nombre nulo/vacío en actualización");
            throw new BadRequestException("ID del rol y nuevo nombre no pueden ser nulos o vacíos");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Intento de actualizar un rol inexistente con ID: {}", id);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + id);
                });

        role.setName(newName);
        Role updatedRole = roleRepository.save(role);
        log.info("Rol con ID {} actualizado correctamente a: {}", id, newName);
        
        return updatedRole;
    }

    /**
     * Asigna un rol a un usuario
     * 
     * @param userId ID del usuario
     * @param roleId ID del rol a asignar
     * @throws BadRequestException si algún ID es nulo
     * @throws ResourceNotFoundException si no existe el usuario o rol
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            log.warn("ID de usuario o rol nulo en intento de asignación");
            throw new BadRequestException("ID de usuario y rol no pueden ser nulos");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Rol con ID {} no encontrado", roleId);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + roleId);
                });

        // Verificar si el usuario ya tiene ese rol
        if (user.getRoles().stream().anyMatch(r -> r.getIdRole().equals(roleId))) {
            log.info("El usuario {} ya tiene asignado el rol {}", userId, roleId);
            return; // No hacer nada si ya tiene el rol
        }

        // Asignar el rol
        user.addRole(role);
        userRepository.save(user);
        log.info("Rol {} asignado correctamente al usuario {}", roleId, userId);
    }

    /**
     * Quita un rol a un usuario
     * 
     * @param userId ID del usuario
     * @param roleId ID del rol a quitar
     * @throws BadRequestException si algún ID es nulo
     * @throws ResourceNotFoundException si no existe el usuario o rol
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            log.warn("ID de usuario o rol nulo en intento de eliminación de rol");
            throw new BadRequestException("ID de usuario y rol no pueden ser nulos");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Rol con ID {} no encontrado", roleId);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + roleId);
                });

        // Verificar si el usuario tiene ese rol
        if (user.getRoles().stream().noneMatch(r -> r.getIdRole().equals(roleId))) {
            log.info("El usuario {} no tiene asignado el rol {}", userId, roleId);
            return; // No hacer nada si no tiene el rol
        }

        // Quitar el rol
        user.removeRole(role);
        userRepository.save(user);
        log.info("Rol {} eliminado correctamente del usuario {}", roleId, userId);
    }

    /**
     * Obtiene todos los roles de un usuario específico
     * 
     * @param userId ID del usuario
     * @return Lista de roles del usuario
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si no existe el usuario
     */
    public List<Role> getRolesByUser(Long userId) {
        if (userId == null) {
            log.warn("ID de usuario nulo");
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        List<Role> roles = user.getRoles();
        log.info("Se encontraron {} roles para el usuario {}", roles.size(), userId);
        
        return roles;
    }

    /**
     * Busca un rol por su nombre
     * 
     * @param roleName nombre del rol
     * @return El rol encontrado
     * @throws BadRequestException si el nombre es nulo o vacío
     * @throws ResourceNotFoundException si no existe el rol
     */
    public Role getRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            log.warn("Nombre de rol nulo o vacío");
            throw new BadRequestException("Nombre de rol no puede ser nulo o vacío");
        }

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.info("Rol con nombre '{}' no encontrado", roleName);
                    return new ResourceNotFoundException("Rol no encontrado con nombre: " + roleName);
                });
    }
}
