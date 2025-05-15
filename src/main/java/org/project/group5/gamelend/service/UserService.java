package org.project.group5.gamelend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.exception.UserNotFoundException;
import org.project.group5.gamelend.mapper.GameMapper;
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

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GameMapper gameMapper;

    /**
     * Obtiene el rol predeterminado para nuevos usuarios
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
     * Busca un usuario por su ID.
     * 
     * @param id El ID del usuario a buscar.
     * @return La entidad User encontrada.
     * @throws ResourceNotFoundException si no se encuentra ningún usuario con ese
     *                                   ID.
     * @throws BadRequestException       si el ID es nulo.
     */
    public User findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Crea o actualiza un usuario.
     * Este método necesita ser revisado. Si es para crear, las validaciones de
     * existencia
     * de publicName y email deben hacerse ANTES de intentar guardar.
     * Si es para actualizar, esas validaciones deben ser diferentes (permitir el
     * mismo publicName/email
     * si pertenecen al usuario que se está actualizando).
     * 
     * @param user El usuario a guardar
     * @return El usuario guardado
     * @throws BadRequestException si hay problemas de validación
     */
    @Transactional
    public User saveUser(User user) {
        if (user == null) {
            throw new BadRequestException("El usuario no puede ser nulo");
        }

        // Si es una creación (ID es nulo) o si el publicName ha cambiado
        if (user.getId() == null ||
                (user.getPublicName() != null && !user.getPublicName()
                        .equals(userRepository.findById(user.getId()).map(User::getPublicName).orElse(null)))) {
            if (user.getPublicName() != null && userRepository.existsByPublicName(user.getPublicName())) {
                log.warn("Ya existe un usuario con el nombre público: {}", user.getPublicName());
                throw new BadRequestException("Ya existe un usuario con ese nombre público");
            }
        }

        // Si es una creación (ID es nulo) o si el email ha cambiado
        if (user.getId() == null ||
                (user.getEmail() != null && !user.getEmail()
                        .equals(userRepository.findById(user.getId()).map(User::getEmail).orElse(null)))) {
            if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
                log.warn("Ya existe un usuario con el email: {}", user.getEmail());
                throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
            }
        }

        try {
            // Encriptar la contraseña si se proporciona una nueva y no está ya encriptada
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else if (user.getId() != null && (user.getPassword() == null || user.getPassword().isEmpty())) {
                // Si es una actualización y no se proporciona contraseña, mantener la existente
                userRepository.findById(user.getId())
                        .ifPresent(existingUser -> user.setPassword(existingUser.getPassword()));
            }

            User savedUser = userRepository.save(user);
            log.info("Usuario guardado correctamente con ID: {}", savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityViolation(e);
            return null; // Unreachable
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

        if (mensaje.contains("public_name_unique") || mensaje.contains("PUBLIC_NAME_UNIQUE")
                || mensaje.contains("uk_public_name")) {
            log.error("Error al guardar usuario: Nombre público duplicado", e);
            throw new BadRequestException("Ya existe un usuario con ese nombre público");
        } else if (mensaje.contains("email_unique") || mensaje.contains("EMAIL_UNIQUE")
                || mensaje.contains("uk_email")) {
            log.error("Error al guardar usuario: Email duplicado", e);
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
        } else {
            log.error("Error al guardar usuario: Violación de integridad de datos no especificada", e);
            throw new BadRequestException("Error al guardar el usuario: Violación de integridad de datos");
        }
    }

    /**
     * Obtiene todos los usuarios
     * 
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
     * Obtiene todos los usuarios como una lista de UserResponseDTO.
     * 
     * @return Lista de UserResponseDTO.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersAsResponseDTO() {
        log.debug("Solicitando todos los usuarios para convertir a DTO");
        List<User> users = userRepository.findAll();
        return userMapper.toResponseDTOList(users);
    }

    /**
     * Obtiene un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws BadRequestException       si el ID es nulo
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
     * Obtiene un usuario por su ID como UserResponseDTO.
     * 
     * @param id ID del usuario.
     * @return UserResponseDTO del usuario.
     * @throws ResourceNotFoundException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByIdAsResponseDTO(Long id) {
        User user = getUserById(id);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Obtiene un usuario por su email
     * 
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws BadRequestException       si el email es nulo o vacío
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
     * Obtiene un usuario por su email como UserResponseDTO.
     * 
     * @param email Email del usuario.
     * @return UserResponseDTO del usuario.
     * @throws ResourceNotFoundException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmailAsResponseDTO(String email) {
        User user = getUserByEmail(email);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Obtiene el perfil de un usuario por su email para UserResponseDTO.
     * Este método puede ser similar a getUserByEmailAsResponseDTO o tener una
     * lógica específica
     * si el "perfil" implica cargar diferentes datos o un DTO diferente.
     * Por ahora, asumimos que es lo mismo que getUserByEmailAsResponseDTO.
     * 
     * @param email Email del usuario.
     * @return UserResponseDTO del perfil del usuario.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileByEmail(String email) {
        return getUserByEmailAsResponseDTO(email);
    }

    /**
     * Obtiene un usuario completo con sus relaciones por ID como UserResponseDTO.
     * 
     * @param id ID del usuario.
     * @return UserResponseDTO del usuario completo.
     ** @throws ResourceNotFoundException si el usuario no existe.
     */

    @Transactional(readOnly = true)
    public UserResponseDTO getCompleteUserByIdAsResponseDTO(Long id) {
        User user = getCompleteUserById(id);
        return userMapper.toResponseDTO(user);
    }

    /**
     * Elimina un usuario por su ID
     * 
     * @param id ID del usuario a eliminar
     * @throws BadRequestException       si el ID es nulo
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional
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
     * 
     * @param id          ID del usuario a actualizar
     * @param userRequest Datos actualizados del usuario
     * @return Usuario actualizado
     * @throws BadRequestException       si el ID o usuario son nulos
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional
    public User updateUser(Long id, User userRequest) {
        if (id == null || userRequest == null) {
            log.warn("ID o datos de usuario nulos en actualización");
            throw new BadRequestException("ID y datos del usuario no pueden ser nulos");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Intento de actualizar un usuario inexistente con ID: {}", id);
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
                });

        if (userRequest.getName() != null)
            existingUser.setName(userRequest.getName());
        if (userRequest.getPublicName() != null) {
            if (!userRequest.getPublicName().equals(existingUser.getPublicName())
                    && userRepository.existsByPublicName(userRequest.getPublicName())) {
                throw new BadRequestException(
                        "El nombre público '" + userRequest.getPublicName() + "' ya está en uso.");
            }
            existingUser.setPublicName(userRequest.getPublicName());
        }
        if (userRequest.getEmail() != null) {
            if (!userRequest.getEmail().equals(existingUser.getEmail())
                    && userRepository.existsByEmail(userRequest.getEmail())) {
                throw new BadRequestException("El email '" + userRequest.getEmail() + "' ya está en uso.");
            }
            existingUser.setEmail(userRequest.getEmail());
        }
        if (userRequest.getProvince() != null)
            existingUser.setProvince(userRequest.getProvince());
        if (userRequest.getCity() != null)
            existingUser.setCity(userRequest.getCity());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            if (!userRequest.getPassword().startsWith("$2a$")) {
                existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            }
        }

        User updated = userRepository.save(existingUser);
        log.info("Usuario con ID {} actualizado correctamente", id);

        return updated;
    }

    /**
     * Obtiene un DTO de usuario por su email
     * 
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

            return new UserDTO(
                    user.getName(),
                    user.getPublicName(),
                    user.getEmail(),
                    user.getProvince(),
                    user.getCity(),
                    null,
                    user.getRegistrationDate(),
                    null,
                    null);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener usuario por email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al obtener el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un DTO de usuario con sus juegos por nombre público
     * 
     * @param publicName Nombre público del usuario
     * @return DTO del usuario con sus juegos
     * @throws UserNotFoundException si el usuario no existe
     * @throws RuntimeException      si ocurre un error inesperado durante la
     *                               obtención de datos.
     */
    @Transactional(readOnly = true)
    public UserDTO getGamesByPublicName(String publicName) {
        try {
            Optional<User> userOpt = userRepository.findByPublicName(publicName);
            if (userOpt.isEmpty()) {
                log.warn("Usuario con nombre público {} no encontrado", publicName);
                throw new UserNotFoundException("Usuario con publicName: " + publicName + " no existe");
            }

            User user = userOpt.get();
            // Forzar la inicialización de la colección de juegos si es lazy
            // y se necesita para el DTO.
            Hibernate.initialize(user.getGames());
            Hibernate.initialize(user.getRoles()); // También inicializar roles si son lazy
            log.debug("Usuario con nombre público {} y {} juegos encontrado", publicName,
                    user.getGames() != null ? user.getGames().size() : 0);

            // Crear el DTO con el orden y número correcto de parámetros.
            // El campo 'password' se pasa como null por seguridad.
            List<org.project.group5.gamelend.dto.GameDTO> gameDTOs = user.getGames() != null
                ? gameMapper.toDTOList(user.getGames())
                : new java.util.ArrayList<>();

            List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
                : new java.util.ArrayList<>();

            return new UserDTO(
                    user.getName(),
                    user.getPublicName(),
                    user.getEmail(),
                    user.getProvince(),
                    user.getCity(),
                    null,
                    user.getRegistrationDate(),
                    gameDTOs,
                    roleNames
            );
        } catch (UserNotFoundException e) {
            // Re-lanzar UserNotFoundException para que sea manejada por el advice del
            // controlador o el llamador.
            throw e;
        } catch (org.hibernate.HibernateException e) { // Captura específica para errores de Hibernate
            log.error("Error de Hibernate al obtener juegos para usuario {}: {}", publicName, e.getMessage(), e);
            throw new RuntimeException("Error de base de datos al obtener juegos del usuario: " + e.getMessage(), e);
        } catch (Exception e) {
            // Captura genérica para cualquier otra excepción inesperada.
            // Es importante loguear esta excepción ya que es inesperada.
            log.error("Error inesperado al obtener juegos para usuario {}: {}", publicName, e.getMessage(), e);
            // Considera si quieres envolverla en una excepción personalizada o una
            // RuntimeException genérica.
            throw new RuntimeException("Error inesperado al obtener juegos del usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * 
     * @return Usuario autenticado
     * @throws AuthenticationCredentialsNotFoundException si el usuario no está
     *                                                    autenticado
     * @throws UsernameNotFoundException                  si el usuario autenticado
     *                                                    no existe en la base de
     *                                                    datos
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
     * Obtiene el usuario actual y lo convierte a DTO
     * 
     * @return DTO del usuario actual
     * @throws AuthenticationCredentialsNotFoundException si el usuario no está
     *                                                    autenticado
     * @throws UsernameNotFoundException                  si el usuario autenticado
     *                                                    no existe en la base de
     *                                                    datos
     */
    public UserResponseDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return userMapper.toResponseDTO(user);
    }

    /**
     * Método adicional para obtener usuario actual sin lanzar excepciones
     * 
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
     * 
     * @param id ID del usuario
     * @return Usuario completo con sus relaciones
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public User getCompleteUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("ID de usuario no puede ser nulo");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getLoansReceived());
        Hibernate.initialize(user.getRoles());

        return user;
    }

    /**
     * Verifica si existe un usuario con el nombre público dado
     * 
     * @param publicName Nombre público a verificar
     * @return true si existe, false si no
     */
    public boolean existsByPublicName(String publicName) {
        if (publicName == null || publicName.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByPublicName(publicName);
    }

    /**
     * Verifica si existe un usuario con el email dado
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    /**
     * Crea un usuario desde un UserDTO.
     * Este método es el que el controlador podría llamar.
     * 
     * @param userDTO DTO con la información del usuario a crear.
     * @return UserResponseDTO del usuario creado.
     */
    @Transactional
    public UserResponseDTO createUserFromDTO(UserDTO userDTO) {
        if (userDTO.publicName() != null && userRepository.existsByPublicName(userDTO.publicName())) {
            throw new BadRequestException("Ya existe un usuario con ese nombre público: " + userDTO.publicName());
        }
        if (userDTO.email() != null && userRepository.existsByEmail(userDTO.email())) {
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico: " + userDTO.email());
        }

        User user = userMapper.toEntity(userDTO);

        if (userDTO.password() == null || userDTO.password().isEmpty()) {
            throw new BadRequestException("La contraseña no puede estar vacía.");
        }
        user.setPassword(passwordEncoder.encode(userDTO.password()));

        user.setRegistrationDate(LocalDateTime.now());

        if (userDTO.roles() == null || userDTO.roles().isEmpty()) {
            user.setRoles(new ArrayList<>(List.of(getDefaultRole())));
        } else {
            List<Role> roles = userDTO.roles().stream()
                    .map(this::findRoleByName)
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }

        user.setGames(new ArrayList<>());

        User savedUser = userRepository.save(user);
        log.info("Usuario creado desde DTO con ID: {}", savedUser.getId());
        return userMapper.toResponseDTO(savedUser);
    }
}
