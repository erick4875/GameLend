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
 * Controlador REST para usuarios.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    // Mensajes de error.
    private static final String ERR_ID_NULL = "User ID cannot be null";
    private static final String ERR_EMAIL_NULL = "Email cannot be null or empty";
    private static final String ERR_USER_DATA_NULL = "User data cannot be null";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ===== OPERACIONES CRUD =====

    /**
     * Obtiene todos los usuarios.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Requesting list of users");
        List<UserResponseDTO> users = userService.getAllUsersAsResponseDTO();

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene un usuario por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Finding user with ID: {}", id);
        validateId(id);
        UserResponseDTO userResponseDTO = userService.getUserByIdAsResponseDTO(id);
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Crea un nuevo usuario (solo admin).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("Creating new user with data: {}", userDTO);
        validateNewUserData(userDTO);

        User user = prepareUserForCreation(userDTO);
        User savedUser = userService.saveUser(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponseDTO(savedUser));
    }

    /**
     * Actualiza un usuario.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        log.info("Updating user with ID: {} with data: {}", id, userDTO);
        validateId(id);

        if (userDTO == null) {
            throw new BadRequestException(ERR_USER_DATA_NULL);
        }

        User existingUser = userService.getUserById(id);
        updateUserFields(existingUser, userDTO);

        User updatedUser = userService.saveUser(existingUser);

        return ResponseEntity.ok(userMapper.toResponseDTO(updatedUser));
    }

    /**
     * Elimina un usuario por ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        validateId(id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // ===== BÚSQUEDAS ESPECIALizadas =====

    /**
     * Obtiene un usuario por email.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Finding user with email: {}", email);
        validateEmail(email);
        UserResponseDTO userResponseDTO = userService.getUserByEmailAsResponseDTO(email);
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Obtiene perfil básico de usuario por email.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getUserProfile(@RequestParam String email) {
        log.info("Requesting profile for user with email: {}", email);
        validateEmail(email);
        UserResponseDTO userProfileDTO = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * Obtiene usuario completo (con relaciones) por ID.
     */
    @GetMapping("/{id}/complete")
    public ResponseEntity<UserResponseDTO> getCompleteUser(@PathVariable Long id) {
        log.info("Finding complete user with ID: {}", id);
        validateId(id);
        UserResponseDTO userResponseDTO = userService.getCompleteUserByIdAsResponseDTO(id);
        return ResponseEntity.ok(userResponseDTO);
    }

    // ===== MÉTODOS AUXILIARES PRIVADOS =====

    /**
     * Valida que el ID no sea nulo.
     */
    private void validateId(Long id) {
        if (id == null) {
            throw new BadRequestException(ERR_ID_NULL);
        }
    }

    /**
     * Valida que el email no sea nulo o vacío.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException(ERR_EMAIL_NULL);
        }
    }

    /**
     * Valida datos para un nuevo usuario.
     */
    private void validateNewUserData(UserDTO userDTO) {
        if (userDTO == null) {
            throw new BadRequestException(ERR_USER_DATA_NULL);
        }
        if (userService.existsByPublicName(userDTO.publicName())) {
            throw new BadRequestException("A user with that public name already exists");
        }

        if (userService.existsByEmail(userDTO.email())) {
            throw new BadRequestException("A user with that email already exists");
        }
    }

    /**
     * Prepara un objeto User para su creación.
     */
    private User prepareUserForCreation(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);

        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setRegistrationDate(LocalDateTime.now());

        if (userDTO.roles() == null || userDTO.roles().isEmpty()) {
            if (user.getRoles() == null) {
                user.setRoles(new java.util.ArrayList<>());
            }
            user.getRoles().add(userService.getDefaultRole());
        } else {
            List<Role> roles = userDTO.roles().stream()
                    .map(roleName -> userService.findRoleByName(roleName))
                    .filter(role -> role != null)
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }
        return user;
    }

    /**
     * Actualiza campos de un usuario existente desde un DTO.
     */
    private void updateUserFields(User existingUser, UserDTO userDTO) {
        userMapper.updateUserFromDto(userDTO, existingUser);

        if (userDTO.password() != null && !userDTO.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.password()));
        }
    }
}
