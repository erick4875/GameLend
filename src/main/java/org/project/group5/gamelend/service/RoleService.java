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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    /**
     * Crea un nuevo rol de usuario.
     * El ID en el RoleDTO de entrada es ignorado.
     *
     * @param roleDTO DTO con el nombre del rol a crear
     * @return El RoleDTO del rol creado
     * @throws BadRequestException si el nombre es nulo o vacío o si ya existe
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
     * Obtiene todos los roles de usuario como DTOs.
     *
     * @return Lista de RoleDTO (puede estar vacía)
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        log.debug("Se encontraron {} roles", roles.size());
        return roleMapper.toDTOList(roles);
    }

    /**
     * Obtiene un rol por su ID como DTO.
     *
     * @param id ID del rol a buscar
     * @return El RoleDTO encontrado
     * @throws BadRequestException       si el ID es nulo
     * @throws ResourceNotFoundException si no existe el rol
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
     * Elimina un rol por su ID.
     *
     * @param id ID del rol a eliminar.
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
     * Actualiza un rol existente.
     *
     * @param id      ID del rol a actualizar.
     * @param roleDTO DTO con el nuevo nombre para el rol.
     * @return El RoleDTO actualizado.
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

    /**
     * Asigna un rol a un usuario.
     *
     * @param userId ID del usuario.
     * @param roleId ID del rol a asignar.
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
     * Quita un rol a un usuario.
     *
     * @param userId ID del usuario
     * @param roleId ID del rol a quitar
     * @throws BadRequestException       si algún ID es nulo
     * @throws ResourceNotFoundException si no existe el usuario o rol
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
     * Obtiene todos los roles de un usuario específico como DTOs.
     *
     * @param userId ID del usuario
     * @return Lista de RoleDTO del usuario
     * @throws BadRequestException       si el ID es nulo
     * @throws ResourceNotFoundException si no existe el usuario
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

    /**
     * Busca un rol por su nombre y lo devuelve como DTO.
     *
     * @param roleName nombre del rol
     * @return El RoleDTO encontrado
     * @throws BadRequestException       si el nombre es nulo o vacío
     * @throws ResourceNotFoundException si no existe el rol
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
