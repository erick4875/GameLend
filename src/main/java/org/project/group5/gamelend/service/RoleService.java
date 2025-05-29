package org.project.group5.gamelend.service;

import java.util.List;

import org.project.group5.gamelend.dto.RoleDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.RoleMapper;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de roles de usuario.
 * Maneja operaciones CRUD y asignación de roles a usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    // === Operaciones CRUD ===

    /**
     * Crea un nuevo rol.
     * @throws BadRequestException si el nombre es nulo/vacío o duplicado
     */
    public RoleDTO createRole(RoleDTO roleDTO) {
        // Valida que el DTO y el nombre no sean nulos o vacíos
        if (roleDTO == null || roleDTO.name() == null || roleDTO.name().trim().isEmpty()) {
            log.warn("Intento de crear rol con DTO nulo o nombre nulo/vacío");
            throw new BadRequestException("Los datos del rol y el nombre no pueden ser nulos o vacíos");
        }
        String name = roleDTO.name().trim();

        // Verifica si ya existe un rol con ese nombre
        if (roleRepository.existsByName(name)) {
            log.info("Intento de crear rol con nombre ya existente: {}", name);
            throw new BadRequestException("Ya existe un rol con el nombre: " + name);
        }

        // Crea y guarda el nuevo rol
        Role role = new Role();
        role.setName(name);
        Role savedRole = roleRepository.save(role);
        log.info("Rol creado correctamente: {}", name);

        return roleMapper.toDTO(savedRole);
    }

    /**
     * Lista todos los roles.
     * @return Lista de roles (puede estar vacía)
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        log.debug("Se encontraron {} roles", roles.size());
        return roleMapper.toDTOList(roles);
    }

    /**
     * Obtiene rol por ID.
     * @throws BadRequestException si ID es nulo
     * @throws ResourceNotFoundException si no existe
     */
    public RoleDTO getRoleById(Long id) {
        if (id == null) {
            log.warn("ID de rol nulo");
            throw new BadRequestException("ID del rol no puede ser nulo");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Rol con ID {} no encontrado", id);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + id);
                });
        return roleMapper.toDTO(role);
    }

    /**
     * Elimina rol por ID.
     * @throws BadRequestException si ID es nulo
     * @throws ResourceNotFoundException si no existe
     */
    public void deleteRole(Long id) {
        // Valida que el ID no sea nulo.
        if (id == null) {
            log.warn("ID de rol nulo en intento de eliminación");
            throw new BadRequestException("ID del rol no puede ser nulo");
        }

        // Verifica si el rol existe antes de intentar eliminarlo.
        if (!roleRepository.existsById(id)) {
            log.info("Intento de eliminar un rol inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }

        // Elimina el rol.
        roleRepository.deleteById(id);
        log.info("Rol con ID {} eliminado correctamente", id);
    }

    /**
     * Actualiza rol existente.
     * @throws BadRequestException si datos son inválidos
     * @throws ResourceNotFoundException si no existe
     */
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        // Valida que el ID y el DTO (y su nombre) no sean nulos o vacíos.
        if (id == null || roleDTO == null || roleDTO.name() == null || roleDTO.name().trim().isEmpty()) {
            log.warn("ID de rol nulo o DTO/nombre nulo/vacío en actualización");
            throw new BadRequestException(
                    "ID del rol y los datos del rol (incluyendo el nombre) no pueden ser nulos o vacíos");
        }
        String newName = roleDTO.name().trim(); // Limpia el nuevo nombre.

        // Busca el rol existente o lanza excepción si no se encuentra.
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Intento de actualizar un rol inexistente con ID: {}", id);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + id);
                });

        // Verifica si el nuevo nombre ya está en uso por otro rol.
        if (!role.getName().equalsIgnoreCase(newName) && roleRepository.existsByName(newName)) {
            log.info("Intento de actualizar rol ID {} a un nombre ya existente: {}", id, newName);
            throw new BadRequestException("Ya existe otro rol con el nombre: " + newName);
        }

        // Actualiza el nombre y guarda el rol.
        role.setName(newName);
        Role updatedRole = roleRepository.save(role);
        log.info("Rol con ID {} actualizado correctamente a: {}", id, newName);

        return roleMapper.toDTO(updatedRole); // Devuelve el DTO del rol actualizado.
    }

    // === Operaciones con Usuarios ===

    /**
     * Asigna rol a usuario.
     * @throws BadRequestException si IDs son nulos
     * @throws ResourceNotFoundException si usuario/rol no existe
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        // Valida que los IDs no sean nulos.
        if (userId == null || roleId == null) {
            log.warn("ID de usuario o rol nulo en intento de asignación");
            throw new BadRequestException("ID de usuario y rol no pueden ser nulos");
        }

        // Busca el usuario o lanza excepción.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado para asignación de rol", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        // Busca el rol o lanza excepción.
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Rol con ID {} no encontrado para asignación a usuario", roleId);
                    return new ResourceNotFoundException("Rol no encontrado con ID: " + roleId);
                });

        // Verifica si el usuario ya tiene el rol.
        if (user.getRoles().stream().anyMatch(r -> r.getIdRole().equals(roleId))) {
            log.info("El usuario {} ya tiene asignado el rol {}", userId, roleId);
            return; // No hace nada si ya lo tiene.
        }

        // Añade el rol al usuario y guarda.
        user.addRole(role);
        userRepository.save(user);
        log.info("Rol {} asignado correctamente al usuario {}", role.getName(), user.getPublicName());
    }

    /**
     * Remueve rol de usuario.
     * @throws BadRequestException si IDs son nulos
     * @throws ResourceNotFoundException si usuario/rol no existe
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        // Valida que los IDs no sean nulos.
        if (userId == null || roleId == null) {
            log.warn("ID de usuario o rol nulo en intento de eliminación de rol de usuario");
            throw new BadRequestException("ID de usuario y rol no pueden ser nulos");
        }

        // Busca el usuario o lanza excepción.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado para eliminación de rol", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        // Busca el rol a eliminar en la lista de roles del usuario.
        Role roleToRemove = user.getRoles().stream()
                .filter(r -> r.getIdRole().equals(roleId))
                .findFirst()
                .orElseThrow(() -> {
                    log.info("El usuario {} no tiene asignado el rol ID {} para eliminar.", user.getPublicName(),
                            roleId);
                    return new ResourceNotFoundException("El usuario no tiene asignado el rol con ID: " + roleId);
                });

        // Elimina el rol y guarda el usuario.
        user.removeRole(roleToRemove);
        userRepository.save(user);
        log.info("Rol {} eliminado correctamente del usuario {}", roleToRemove.getName(), user.getPublicName());
    }

    /**
     * Lista roles de un usuario.
     * @throws BadRequestException si ID es nulo
     * @throws ResourceNotFoundException si usuario no existe
     */
    public List<RoleDTO> getRolesByUser(Long userId) {
        // Valida que el ID no sea nulo.
        if (userId == null) {
            log.warn("ID de usuario nulo al solicitar roles");
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        // Busca el usuario o lanza excepción.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario con ID {} no encontrado al solicitar roles", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        // Obtiene los roles del usuario y los convierte a DTO.
        List<Role> roles = user.getRoles();
        log.info("Se encontraron {} roles para el usuario {}", roles.size(), userId);

        return roleMapper.toDTOList(roles);
    }

    // === Operaciones de Búsqueda ===

    /**
     * Busca rol por nombre.
     * @throws BadRequestException si nombre es nulo/vacío
     * @throws ResourceNotFoundException si no existe
     */
    public RoleDTO getRoleByName(String roleName) {
        // Valida que el nombre no sea nulo o vacío.
        if (roleName == null || roleName.trim().isEmpty()) {
            log.warn("Nombre de rol nulo o vacío en búsqueda");
            throw new BadRequestException("Nombre de rol no puede ser nulo o vacío");
        }
        String name = roleName.trim();

        // Busca el rol por nombre o lanza excepción si no existe.
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.info("Rol con nombre '{}' no encontrado", name);
                    return new ResourceNotFoundException("Rol no encontrado con nombre: " + name);
                });
        return roleMapper.toDTO(role);
    }
}
