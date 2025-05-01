package org.project.group5.gamelend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.mapper.UserMapper;
import org.project.group5.gamelend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    // Constantes para mensajes de error
    private static final String ERR_ID_NULL = "User ID cannot be null";
    private static final String ERR_EMAIL_NULL = "Email cannot be null or empty";
    private static final String ERR_USER_DATA_NULL = "User data cannot be null";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ===== OPERACIONES BÁSICAS CRUD =====

    /**
     * Obtiene todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Requesting list of users");
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(userMapper.toResponseDTOList(users));
    }

    /**
     * Obtiene un usuario por su ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Finding user with ID: {}", id);
        validateId(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    /**
     * Crea un nuevo usuario (solo admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("Creating new user");
        validateNewUserData(userDTO);

        User user = prepareUserForCreation(userDTO);
        User savedUser = userService.saveUser(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponseDTO(savedUser));
    }

    /**
     * Actualiza un usuario existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        validateId(id);

        if (userDTO == null) {
            throw new BadRequestException(ERR_USER_DATA_NULL);
        }

        User existingUser = userService.getUserById(id);
        updateUserFields(existingUser, userDTO);

        User updatedUser = userService.updateUser(id, existingUser);
        return ResponseEntity.ok(userMapper.toResponseDTO(updatedUser));
    }

    /**
     * Elimina un usuario por su ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        validateId(id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // ===== BÚSQUEDAS ESPECIALIZADAS =====

    /**
     * Obtiene un usuario por su email
     */
    @GetMapping("/email/{email}")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Finding user with email: {}", email);
        validateEmail(email);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    /**
     * Obtiene el perfil básico de un usuario por su email
     */
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDTO> getUserProfile(@RequestParam String email) {
        log.info("Requesting profile for user with email: {}", email);
        validateEmail(email);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    /**
     * Obtiene un usuario completo con sus relaciones por ID
     */
    @GetMapping("/{id}/complete")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDTO> getCompleteUser(@PathVariable Long id) {
        log.info("Finding complete user with ID: {}", id);
        validateId(id);
        User user = userService.getCompleteUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    // ===== MÉTODOS AUXILIARES PRIVADOS =====

    /**
     * Valida que el ID de usuario no sea nulo
     */
    private void validateId(Long id) {
        if (id == null) {
            throw new BadRequestException(ERR_ID_NULL);
        }
    }

    /**
     * Valida que el email no sea nulo o vacío
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException(ERR_EMAIL_NULL);
        }
    }

    /**
     * Valida los datos para un nuevo usuario
     */
    private void validateNewUserData(UserDTO userDTO) {
        if (userDTO == null) {
            throw new BadRequestException(ERR_USER_DATA_NULL);
        }

        if (userService.existsByPublicName(userDTO.getPublicName())) {
            throw new BadRequestException("A user with that public name already exists");
        }

        if (userService.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("A user with that email already exists");
        }
    }

    /**
     * Prepara un objeto User para creación a partir de un DTO
     * permite crear al usuario con un rol por defecto si no se especifica ninguno
     * permite asignar roles de administrador si se especifican
     */
    private User prepareUserForCreation(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);

        // Campos adicionales
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());

        // Roles
        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            user.getRoles().add(userService.getDefaultRole());
        } else {
            List<Role> roles = userDTO.getRoles().stream()
                    .map(roleName -> userService.findRoleByName(roleName))
                    .filter(role -> role != null)
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }

        return user;
    }

    /**
     * Actualiza los campos de un usuario existente
     */
    private void updateUserFields(User existingUser, UserDTO userDTO) {
        // Actualizar los campos con el mapper
        userMapper.updateUserFromDto(userDTO, existingUser);

        // Actualizar la contraseña si se proporcionó una nueva
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
    }
}
