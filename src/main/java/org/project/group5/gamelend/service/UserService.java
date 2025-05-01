package org.project.group5.gamelend.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.exception.UserNotFoundException;
import org.project.group5.gamelend.mapper.UserMapper;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de usuarios
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository userRoleRepository;
    private final UserMapper userMapper;

    /**
     * Obtiene el rol predeterminado para nuevos usuarios
     */
    public Role getDefaultRole() {
        return userRoleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Rol ROLE_USER no encontrado"));
    }

    /**
     * Encuentra un rol por su nombre
     */
    public Role findRoleByName(String roleName) {
        return userRoleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Rol " + roleName + " no encontrado"));
    }

    /**
     * Crea o actualiza un usuario
     * 
     * @param user El usuario a guardar
     * @return El usuario guardado
     * @throws BadRequestException si hay problemas de validación
     */
    public User saveUser(User user) {
        if (user == null) {
            throw new BadRequestException("El usuario no puede ser nulo");
        }

        // Verificar si ya existe un usuario con el mismo publicName o email
        if (existsByPublicName(user.getPublicName())) {
            log.warn("Ya existe un usuario con el nombre público: {}", user.getPublicName());
            throw new BadRequestException("Ya existe un usuario con ese nombre público");
        }

        if (existsByEmail(user.getEmail())) {
            log.warn("Ya existe un usuario con el email: {}", user.getEmail());
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
        }

        try {
            // Encriptar la contraseña si no está encriptada ya
            if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Guardar el usuario
            User savedUser = userRepository.save(user);
            log.info("Usuario guardado correctamente con ID: {}", savedUser.getId());

            return savedUser;
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityViolation(e);
            // Esta línea nunca se ejecutará porque handleDataIntegrityViolation siempre lanza una excepción
            return null;
        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Maneja errores de integridad de datos
     */
    private void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "";

        if (mensaje.contains("public_name")) {
            log.error("Error al guardar usuario: Nombre público duplicado", e);
            throw new BadRequestException("Ya existe un usuario con ese nombre público");
        } else if (mensaje.contains("email")) {
            log.error("Error al guardar usuario: Email duplicado", e);
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
        } else {
            log.error("Error al guardar usuario: Violación de integridad de datos", e);
            throw new BadRequestException("Error al guardar el usuario: Violación de integridad de datos");
        }
    }

    /**
     * Obtiene todos los usuarios
     * @return Lista de usuarios
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.info("No se encontraron usuarios");
        }
        
        log.debug("Se encontraron {} usuarios", users.size());
        return users;
    }

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public User getUserById(Long id) {
        if (id == null) {
            log.warn("ID de usuario nulo");
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        return userRepository.findById(id)
            .orElseThrow(() -> {
                log.info("Usuario con ID {} no encontrado", id);
                return new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
            });
    }

    /**
     * Obtiene un usuario por su email
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws BadRequestException si el email es nulo o vacío
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email de usuario nulo o vacío");
            throw new BadRequestException("Email de usuario no puede ser nulo o vacío");
        }

        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.info("Usuario con email {} no encontrado", email);
                return new ResourceNotFoundException("Usuario no encontrado con email: " + email);
            });
    }

    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @throws BadRequestException si el ID es nulo
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public void deleteUser(Long id) {
        if (id == null) {
            log.warn("ID de usuario nulo en intento de eliminación");
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        if (!userRepository.existsById(id)) {
            log.info("Intento de eliminar un usuario inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("Usuario con ID {} eliminado correctamente", id);
    }

    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param user Datos actualizados del usuario
     * @return Usuario actualizado
     * @throws BadRequestException si el ID o usuario son nulos
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public User updateUser(Long id, User user) {
        if (id == null || user == null) {
            log.warn("ID o datos de usuario nulos en actualización");
            throw new BadRequestException("ID y datos del usuario no pueden ser nulos");
        }

        if (!userRepository.existsById(id)) {
            log.info("Intento de actualizar un usuario inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        
        user.setId(id);
        User updated = userRepository.save(user);
        log.info("Usuario con ID {} actualizado correctamente", id);
        
        return updated;
    }

    /**
     * Obtiene un DTO de usuario por su email
     * @param email Email del usuario
     * @return DTO del usuario
     * @throws UserNotFoundException si el usuario no existe
     */
    public UserDTO getUserBasicByEmailDTO(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("Usuario con email {} no encontrado", email);
                throw new UserNotFoundException("Usuario con email: " + email + " no encontrado");
            }

            log.debug("Usuario con email {} encontrado correctamente", email);
            User user = userOpt.get();

            return UserDTO.builder()
                    .publicName(user.getPublicName())
                    .email(user.getEmail())
                    .registrationDate(user.getRegistrationDate().toString())
                    .build();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener usuario por email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al obtener el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un DTO de usuario con sus juegos por nombre público
     * @param publicName Nombre público del usuario
     * @return DTO del usuario con sus juegos
     * @throws UserNotFoundException si el usuario no existe
     */
    public UserDTO getGamesByPublicName(String publicName) {
        try {
            Optional<User> userOpt = userRepository.findByPublicName(publicName);
            if (userOpt.isEmpty()) {
                log.warn("Usuario con nombre público {} no encontrado", publicName);
                throw new UserNotFoundException("Usuario con publicName: " + publicName + " no existe");
            }

            User user = userOpt.get();
            log.debug("Usuario con nombre público {} y {} juegos encontrado", publicName, user.getGames().size());

            return UserDTO.builder()
                    .publicName(user.getPublicName())
                    .games(user.getGames())
                    .build();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener juegos para usuario {}: {}", publicName, e.getMessage(), e);
            throw new RuntimeException("Error al obtener juegos del usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario autenticado
     * @throws AuthenticationCredentialsNotFoundException si el usuario no está autenticado
     * @throws UsernameNotFoundException si el usuario autenticado no existe en la base de datos
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }
        return userOpt.get();
    }

    /**
     * Obtiene todos los usuarios como DTOs
     * @return Lista de DTOs de usuarios
     */
    public List<UserResponseDTO> getAllUsersDTO() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseDTOList(users);
    }

    /**
     * Obtiene un usuario completo por su ID como DTO
     * @param id ID del usuario
     * @return DTO del usuario completo
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCompleteUserDTO(Long id) {
        User user = getCompleteUserById(id);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Obtiene un usuario por su ID como DTO
     * @param id ID del usuario
     * @return DTO del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UserResponseDTO getUserByIdDTO(Long id) {
        User user = getUserById(id);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Obtiene un usuario por su email como DTO
     * @param email Email del usuario
     * @return DTO del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UserResponseDTO getUserByEmailDTO(String email) {
        User user = getUserByEmail(email);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Actualiza un usuario y devuelve el DTO actualizado
     * @param id ID del usuario a actualizar
     * @param user Datos actualizados del usuario
     * @return DTO del usuario actualizado
     */
    public UserResponseDTO updateUserDTO(Long id, User user) {
        User updatedUser = updateUser(id, user);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Guarda un usuario y devuelve el DTO
     * @param user Usuario a guardar
     * @return DTO del usuario guardado
     */
    public UserResponseDTO saveUserDTO(User user) {
        User savedUser = saveUser(user);
        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * Obtiene el usuario actual y lo convierte a DTO
     * @return DTO del usuario actual
     * @throws UnauthorizedException si el usuario no está autenticado
     * @throws UserNotFoundException si el usuario autenticado no existe en la base de datos
     */
    public UserResponseDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return userMapper.toResponseDTO(user);
    }

    /**
     * Método adicional para obtener usuario actual sin lanzar excepciones
     * @return DTO del usuario actual o null si hay error
     */
    public UserResponseDTO getCurrentUserDTOSafe() {
        try {
            User user = getCurrentUser();
            return userMapper.toResponseDTO(user);
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario actual: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene un usuario completo con todos sus datos relacionados por su ID
     * @param id ID del usuario
     * @return Usuario completo con sus relaciones
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public User getCompleteUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        User user = userRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
            
        // Inicializar colecciones lazy si no se han cargado con la consulta
        Hibernate.initialize(user.getGames());
        
        return user;
    }

    /**
     * Verifica si existe un usuario con el nombre público dado
     * @param publicName Nombre público a verificar
     * @return true si existe, false si no
     */
    public boolean existsByPublicName(String publicName) {
        if (publicName == null || publicName.trim().isEmpty()) {
            return false;
        }

        try {
            return userRepository.existsByPublicName(publicName);
        } catch (Exception e) {
            log.error("Error al verificar existencia de usuario por nombre público: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("Error al verificar existencia de usuario por email: {}", e.getMessage(), e);
            return false;
        }
    }
}
