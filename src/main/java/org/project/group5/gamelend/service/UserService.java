package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.UserMapper;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository; // Asegúrate de que esté inyectado si getGamesByPublicName lo usa
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service; // Para el try-catch en saveUser
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder; // Necesario si saveUser hashea contraseñas
    private final RoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Role getDefaultRole() {
        return userRoleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ROLE_USER no encontrado"));
    }

    public Role findRoleByName(String roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol " + roleName + " no encontrado"));
    }

    public User findUserByEmail(String email) { // Usado por UserController y potencialmente por AuthService/Security
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("El email de usuario no puede ser nulo o vacío.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    public User getUserById(Long id) { // Cambiado de findById para consistencia con otros getters
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Guarda un NUEVO usuario. Las validaciones de unicidad son para creación.
     */
    @Transactional
    public User saveUser(User user) { // Este user ya viene preparado desde el controller
        if (user == null) {
            throw new BadRequestException("El usuario no puede ser nulo");
        }

        try {
            // La contraseña ya debería estar encriptada por prepareUserForCreation
            // La fecha de registro ya debería estar establecida por prepareUserForCreation
            User savedUser = userRepository.save(user);
            log.info("Usuario guardado/actualizado correctamente con ID: {}", savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            // Este catch es bueno para proporcionar mensajes de error más específicos
            // si las validaciones previas fallaron o si hay otras restricciones de BD.
            log.error("Error de integridad de datos al guardar usuario: {}", e.getMessage());
            handleDataIntegrityViolation(e); // Lanza excepciones específicas
            return null; // No se alcanzará debido al throw en handleDataIntegrityViolation
        } catch (Exception e) {
            log.error("Error inesperado al guardar usuario", e);
            throw new RuntimeException("Error inesperado al guardar el usuario: " + e.getMessage(), e);
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String mensaje = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        // Ajusta estas strings para que coincidan con los nombres de tus constraints
        // UNIQUE en la BD
        if (mensaje.contains("public_name")) { // O el nombre de tu constraint UNIQUE para public_name
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre público ya está en uso.");
        } else if (mensaje.contains("email")) { // O el nombre de tu constraint UNIQUE para email
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede completar la operación debido a restricciones de datos.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersAsResponseDTO() {
        log.debug("Solicitando todos los usuarios para convertir a DTO");
        List<User> users = userRepository.findAll();
        // la transaccion se mantiene abierta permitiendo que el mapper acceda a las entidades
        return userMapper.toResponseDTOList(users);
    }

    public UserResponseDTO getUserByIdAsResponseDTO(Long id) {
        User user = getUserById(id); // Reutiliza el método que ya valida y lanza ResourceNotFoundException
        return userMapper.toResponseDTO(user);
    }

    public UserResponseDTO getUserByEmailAsResponseDTO(String email) {
        User user = findUserByEmail(email); // Reutiliza el método que ya valida
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return userMapper.toResponseDTO(user);
    }

    // === MÉTODO REFINADO PARA ACTUALIZAR PERFIL ===
    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UserDTO userUpdateDTO) {
        log.info("Intentando actualizar perfil para userId: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con ID: {} para actualización.", userId);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        boolean changesMade = false;

        // Actualizar Name (nombre real)
        if (userUpdateDTO.name() != null && !userUpdateDTO.name().isBlank()) {
            if (!userUpdateDTO.name().equals(userToUpdate.getName())) {
                userToUpdate.setName(userUpdateDTO.name());
                changesMade = true;
                log.debug("UserID: {}, Name actualizado a: {}", userId, userUpdateDTO.name());
            }
        }

        // Actualizar PublicName
        if (userUpdateDTO.publicName() != null && !userUpdateDTO.publicName().isBlank()) {
            if (!userUpdateDTO.publicName().equals(userToUpdate.getPublicName())) {
                if (userRepository.existsByPublicNameAndIdNot(userUpdateDTO.publicName(), userId)) {
                    log.warn("Conflicto para UserID: {}: publicName '{}' ya está en uso por otro usuario.", userId,
                            userUpdateDTO.publicName());
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El nombre público '" + userUpdateDTO.publicName() + "' ya está en uso.");
                }
                userToUpdate.setPublicName(userUpdateDTO.publicName());
                changesMade = true;
                log.debug("UserID: {}, PublicName actualizado a: {}", userId, userUpdateDTO.publicName());
            }
        }

        // Actualizar Provincia
        if (userUpdateDTO.province() != null) {
            String newProvince = userUpdateDTO.province().isBlank() ? null : userUpdateDTO.province();
            if ((newProvince == null && userToUpdate.getProvince() != null) ||
                    (newProvince != null && !newProvince.equals(userToUpdate.getProvince()))) {
                userToUpdate.setProvince(newProvince);
                changesMade = true;
                log.debug("UserID: {}, Province actualizada a: {}", userId, newProvince);
            }
        }

        // Actualizar Ciudad
        if (userUpdateDTO.city() != null) {
            String newCity = userUpdateDTO.city().isBlank() ? null : userUpdateDTO.city();
            if ((newCity == null && userToUpdate.getCity() != null) ||
                    (newCity != null && !newCity.equals(userToUpdate.getCity()))) {
                userToUpdate.setCity(newCity);
                changesMade = true;
                log.debug("UserID: {}, City actualizada a: {}", userId, newCity);
            }
        }

        // Actualizar password
        if (userUpdateDTO.password() != null && !userUpdateDTO.password().isBlank()) {
            String encodedPassword = passwordEncoder.encode(userUpdateDTO.password());
            if (!encodedPassword.equals(userToUpdate.getPassword())) {
                userToUpdate.setPassword(encodedPassword);
                changesMade = true;
                log.debug("UserID: {}, Password actualizado.");
            }
        }

        if (changesMade) {
            User updatedUser = userRepository.save(userToUpdate); // Solo guarda si hubo cambios
            log.info("Perfil de usuario actualizado exitosamente para ID: {}", userId);
            return userMapper.toResponseDTO(updatedUser);
        } else {
            log.info("No se realizaron cambios en el perfil para userId: {}", userId);
            return userMapper.toResponseDTO(userToUpdate); // Devuelve los datos existentes sin guardar
        }
    }

    public UserResponseDTO getCompleteUserByIdAsResponseDTO(Long id) {
        User user = getCompleteUserById(id);
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("Usuario con ID {} eliminado correctamente", id);
    }

    @Transactional(readOnly = true)
    public User getCompleteUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getLoansReceived());
        Hibernate.initialize(user.getRoles());
        return user;
    }

    public boolean existsByPublicName(String publicName) {
        if (publicName == null || publicName.trim().isEmpty())
            return false;
        return userRepository.existsByPublicName(publicName);
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return false;
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserResponseDTO createUserFromDTO(UserDTO userDTO) {
        // ... (tu lógica existente)
        // Asegúrate que las validaciones de unicidad aquí sean para CREACIÓN.
        if (userDTO.publicName() != null && userRepository.existsByPublicName(userDTO.publicName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese nombre público: " + userDTO.publicName());
        }
        if (userDTO.email() != null && userRepository.existsByEmail(userDTO.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo electrónico: " + userDTO.email());
        }

        User user = userMapper.toEntity(userDTO); // UserMapper debe ignorar password, roles, etc.
        // ... (resto de tu lógica de createUserFromDTO)
        if (userDTO.password() == null || userDTO.password().isEmpty()) {
            throw new BadRequestException("La contraseña no puede estar vacía.");
        }
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setRegistrationDate(LocalDateTime.now());
        if (user.getRoles() == null || user.getRoles().isEmpty()) { // Si el mapper no puso roles (debería ignorarlos)
            user.setRoles(new ArrayList<>(List.of(getDefaultRole())));
        }
        // ...
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }
}
