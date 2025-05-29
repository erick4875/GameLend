package org.project.group5.gamelend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.project.group5.gamelend.dto.DocumentUploadDTO;
import org.project.group5.gamelend.dto.UserDTO;
import org.project.group5.gamelend.dto.UserResponseDTO;
import org.project.group5.gamelend.entity.Document;
import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.exception.BadRequestException;
import org.project.group5.gamelend.exception.ResourceNotFoundException;
import org.project.group5.gamelend.mapper.UserMapper;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de usuarios.
 * Maneja operaciones CRUD, autenticación y gestión de perfiles.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DocumentService documentService;


    /**
     * === Operaciones con Roles ===
     */

    /**
     * Obtiene el rol por defecto (ROLE_USER)
     */
    public Role getDefaultRole() {
        return userRoleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ROLE_USER no encontrado"));
    }

    /**
     * Busca un rol por su nombre
     */
    public Role findRoleByName(String roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol " + roleName + " no encontrado"));
    }

    /**
     * === Operaciones CRUD Básicas ===
     */

    /**
     * Busca un usuario por email
     * @throws BadRequestException si el email es nulo o vacío
     * @throws ResourceNotFoundException si no se encuentra el usuario
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("El email de usuario no puede ser nulo o vacío.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public User saveUser(User user) { // Para creación de usuario, usualmente llamado desde AuthService o un método
                                      // createUser
        if (user == null) {
            throw new BadRequestException("El usuario no puede ser nulo");
        }
        try {
            // Asumimos que la contraseña ya está encriptada y la fecha de registro
            // establecida
            User savedUser = userRepository.save(user);
            log.info("Usuario guardado correctamente con ID: {}", savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad de datos al guardar usuario: {}", e.getMessage());
            handleDataIntegrityViolation(e);
            return null;
        } catch (Exception e) {
            log.error("Error inesperado al guardar usuario", e);
            throw new RuntimeException("Error inesperado al guardar el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Maneja errores de integridad de datos
     * @throws ResponseStatusException con mensaje específico según el error
     */
    private void handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String mensaje = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (mensaje.contains("public_name")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre público ya está en uso.");
        } else if (mensaje.contains("email")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso.");
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede completar la operación debido a restricciones de datos.");
        }
    }

    /**
     * === Operaciones de Lectura para DTOs ===
     */

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersAsResponseDTO() {
        log.debug("Solicitando todos los usuarios para convertir a DTO");
        List<User> users = userRepository.findAll();
        // Asegurar que las relaciones LAZY necesarias para UserResponseDTO se carguen
        // para cada usuario
        users.forEach(user -> {
            Hibernate.initialize(user.getProfileImage());
            Hibernate.initialize(user.getGames());
            Hibernate.initialize(user.getLoansMade());
            Hibernate.initialize(user.getRoles());
        });
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByIdAsResponseDTO(Long id) {
        User user = getUserById(id); // Este método ya obtiene el User
        Hibernate.initialize(user.getProfileImage());
        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getRoles());
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmailAsResponseDTO(String email) {
        User user = findUserByEmail(email); // Este método ya obtiene el User
        Hibernate.initialize(user.getProfileImage());
        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getRoles());
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        Hibernate.initialize(user.getProfileImage());
        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getRoles());
        return userMapper.toResponseDTO(user);
    }

    /**
     * === Operaciones de Actualización ===
     */

    /**
     * Actualiza el perfil de un usuario
     * @param userId ID del usuario a actualizar
     * @param userUpdateDTO Datos nuevos
     * @throws ResourceNotFoundException si no existe el usuario
     * @throws ResponseStatusException si hay conflicto de nombres
     */
    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UserDTO userUpdateDTO) {
        log.info("UserService: Actualizando perfil de texto para userId: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId + " para actualización de perfil."));

        boolean changesMade = false;
        if (userUpdateDTO.name() != null && !userUpdateDTO.name().isBlank()
                && !userUpdateDTO.name().equals(userToUpdate.getName())) {
            userToUpdate.setName(userUpdateDTO.name());
            changesMade = true;
        }
        if (userUpdateDTO.publicName() != null && !userUpdateDTO.publicName().isBlank()
                && !userUpdateDTO.publicName().equals(userToUpdate.getPublicName())) {
            if (userRepository.existsByPublicNameAndIdNot(userUpdateDTO.publicName(), userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El nombre público '" + userUpdateDTO.publicName() + "' ya está en uso.");
            }
            userToUpdate.setPublicName(userUpdateDTO.publicName());
            changesMade = true;
        }
        if (userUpdateDTO.province() != null) {
            String newProvince = userUpdateDTO.province().isBlank() ? null : userUpdateDTO.province();
            if ((newProvince == null && userToUpdate.getProvince() != null)
                    || (newProvince != null && !newProvince.equals(userToUpdate.getProvince()))) {
                userToUpdate.setProvince(newProvince);
                changesMade = true;
            }
        }
        if (userUpdateDTO.city() != null) {
            String newCity = userUpdateDTO.city().isBlank() ? null : userUpdateDTO.city();
            if ((newCity == null && userToUpdate.getCity() != null)
                    || (newCity != null && !newCity.equals(userToUpdate.getCity()))) {
                userToUpdate.setCity(newCity);
                changesMade = true;
            }
        }
        if (userUpdateDTO.password() != null && !userUpdateDTO.password().isBlank()) {
            if (!passwordEncoder.matches(userUpdateDTO.password(), userToUpdate.getPassword())) {
                userToUpdate.setPassword(passwordEncoder.encode(userUpdateDTO.password()));
                changesMade = true;
                log.info("UserID: {}, Contraseña actualizada en updateUserProfile.", userId);
            }
        }

        User finalUserToMap;
        if (changesMade) {
            finalUserToMap = userRepository.save(userToUpdate);
            log.info("Datos de texto del perfil actualizados y guardados para userId: {}", userId);
        } else {
        
            finalUserToMap = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId
                            + " después de intento de actualización sin cambios."));
            log.info("No se realizaron cambios de texto en el perfil para userId: {}, pero se recargó el usuario.",
                    userId);
        }

        Hibernate.initialize(finalUserToMap.getProfileImage());
        Hibernate.initialize(finalUserToMap.getGames());
        Hibernate.initialize(finalUserToMap.getLoansMade());
        Hibernate.initialize(finalUserToMap.getRoles());

        return userMapper.toResponseDTO(finalUserToMap);
    }

    /**
     * Establece o actualiza la imagen de perfil para un usuario.
     * Si ya existe una imagen de perfil, se elimina la anterior (archivo físico y
     * registro de BD).
     * 
     * @param userId                   El ID del usuario.
     * @param file                     El archivo de imagen a subir.
     * @param uploadDTO_fromController DTO con metadatos (puede ser null si el
     *                                 controlador no lo envía).
     * @return UserResponseDTO con la información actualizada del usuario.
     * @throws IOException Si hay un error al guardar el archivo.
     */
    @Transactional
    public UserResponseDTO setProfileImage(Long userId, MultipartFile file, DocumentUploadDTO uploadDTO_fromController)
            throws IOException {
        log.info("UserService: Estableciendo imagen de perfil para userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo de imagen no puede estar vacío.");
        }

        Document oldProfileImage = user.getProfileImage();
        if (oldProfileImage != null) {
            log.debug("Eliminando imagen de perfil anterior (ID: {}) para userId: {}", oldProfileImage.getId(), userId);
            user.setProfileImage(null);
            userRepository.saveAndFlush(user);
            documentService.deleteDocument(oldProfileImage.getId());
            log.debug("Imagen de perfil anterior (Documento ID: {}) eliminada.", oldProfileImage.getId());
        }

        DocumentUploadDTO imageUploadDetails;
        if (uploadDTO_fromController != null && uploadDTO_fromController.name() != null
                && !uploadDTO_fromController.name().isBlank()) {
            imageUploadDetails = uploadDTO_fromController;
        } else {
            String defaultName = "profile_image_user_" + userId + "_" + System.currentTimeMillis();
            String defaultDescription = "Imagen de perfil para usuario " + user.getPublicName();
            imageUploadDetails = new DocumentUploadDTO(defaultName, defaultDescription);
            log.debug("DocumentUploadDTO no proporcionado o nombre vacío, usando por defecto: {}", defaultName);
        }

        Document newProfileDocument = documentService.save(file, imageUploadDetails);
        log.info("Documento guardado por DocumentService. ID: {}, Nombre: {}", newProfileDocument.getId(),
                newProfileDocument.getName());

        user.setProfileImage(newProfileDocument);
        User updatedUser = userRepository.save(user); // O saveAndFlush(user)
        log.info("Usuario actualizado con nueva imagen. User ID: {}, ProfileImage ID: {}", updatedUser.getId(),
                (updatedUser.getProfileImage() != null ? updatedUser.getProfileImage().getId() : "null"));

        Hibernate.initialize(updatedUser.getProfileImage());
        UserResponseDTO responseDTO = userMapper.toResponseDTO(updatedUser);
        log.info("UserResponseDTO a devolver: {}", responseDTO); // Verifica profileImageUrl aquí
        return responseDTO;
    }

    /**
     * === Operaciones de Eliminación ===
     */

    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del usuario no puede ser nulo.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("Usuario con ID {} eliminado correctamente", id);
    }

    /**
     * === Consultas Completas de Usuario ===
     */

    @Transactional(readOnly = true)
    public User getCompleteUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        Hibernate.initialize(user.getProfileImage());
        Hibernate.initialize(user.getGames());
        Hibernate.initialize(user.getLoansMade());
        Hibernate.initialize(user.getLoansReceived());
        Hibernate.initialize(user.getRoles());
        return user;
    }

    /**
     * Obtiene un usuario completo por ID y lo convierte a DTO
     * Incluye todas las relaciones (imagen, juegos, préstamos, roles)
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCompleteUserByIdAsResponseDTO(Long id) {
        User user = getCompleteUserById(id);
        return userMapper.toResponseDTO(user);
    }

    /**
     * === Utilidades y Validaciones ===
     */

    /**
     * Verifica existencia de nombre público
     */
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
        if (userDTO.publicName() != null && userRepository.existsByPublicName(userDTO.publicName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese nombre público: " + userDTO.publicName());
        }
        if (userDTO.email() != null && userRepository.existsByEmail(userDTO.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo electrónico: " + userDTO.email());
        }

        User user = userMapper.toEntity(userDTO);

        if (userDTO.password() == null || userDTO.password().isEmpty()) {
            throw new BadRequestException("La contraseña no puede estar vacía.");
        }
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setRegistrationDate(LocalDateTime.now());

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new ArrayList<>(List.of(getDefaultRole())));
        }
        user.setGames(new ArrayList<>()); // Inicializar la lista de juegos
        user.setLoansMade(new ArrayList<>()); // Inicializar
        user.setLoansReceived(new ArrayList<>()); // Inicializar
        // profileImage se establece por separado

        User savedUser = userRepository.save(user);
        log.info("Usuario creado desde DTO con ID: {}", savedUser.getId());
        // Asegurar que las relaciones estén cargadas para el DTO de respuesta
        Hibernate.initialize(savedUser.getProfileImage());
        Hibernate.initialize(savedUser.getGames());
        Hibernate.initialize(savedUser.getLoansMade());
        Hibernate.initialize(savedUser.getRoles());
        return userMapper.toResponseDTO(savedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
