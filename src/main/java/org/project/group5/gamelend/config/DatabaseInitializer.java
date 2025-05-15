package org.project.group5.gamelend.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.entity.User;
import org.project.group5.gamelend.repository.RoleRepository;
import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Configuration: Indica que esta clase define beans de configuración para Spring.
 * @RequiredArgsConstructor: Lombok genera un constructor para los campos 'final'.
 * @Slf4j: Lombok genera un logger para registrar mensajes.
 *
 * Esta clase inicializa datos básicos en la base de datos al arrancar la aplicación,
 * como roles y un usuario administrador por defecto.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    // Repositorios para interactuar con las tablas 'roles' y 'users'.
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    // Servicio para codificar contraseñas.
    private final PasswordEncoder passwordEncoder;

    /**
     * @Bean: Define un 'CommandLineRunner'.
     * CommandLineRunner: Ejecuta código después de que Spring Boot se haya iniciado.
     *                    Ideal para tareas de inicialización.
     * @Order(1): Define el orden de ejecución si hay varios CommandLineRunners.
     * 
     * @return Un CommandLineRunner que inicializa la base de datos.
     */
    @Bean
    @Order(1)
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Inicializando roles y usuario administrador...");
            
            // --- Creación/Verificación de Roles ---
            // Busca el rol "ROLE_USER". Si no existe, lo crea.
            Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role newUserRole = new Role();
                newUserRole.setName("ROLE_USER");
                log.info("Creando rol ROLE_USER.");
                return roleRepository.save(newUserRole);
            });
            
            // Busca el rol "ROLE_ADMIN". Si no existe, lo crea.
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role newAdminRole = new Role();
                newAdminRole.setName("ROLE_ADMIN");
                log.info("Creando rol ROLE_ADMIN.");
                return roleRepository.save(newAdminRole);
            });

            // --- Creación del Usuario Administrador por defecto ---
            String adminEmail = "admin@linkiafp.com";
            // Si no existe un usuario con este email, lo crea.
            if (!userRepository.existsByEmail(adminEmail)) {
                log.info("Creando usuario administrador: {}", adminEmail);
                User adminUser = new User();
                adminUser.setName("Administrador LinkiaFP");
                adminUser.setPublicName("linkiaFPAdmin");
                adminUser.setEmail(adminEmail);
                // Codifica la contraseña antes de guardarla.
                adminUser.setPassword(passwordEncoder.encode("linkiaFP"));
                adminUser.setProvince("Barcelona"); 
                adminUser.setCity("Badalona");
                adminUser.setRegistrationDate(LocalDateTime.now());

                // Asigna los roles de ADMIN y USER al administrador.
                List<Role> adminRolesList = new ArrayList<>();
                adminRolesList.add(adminRole);
                adminRolesList.add(userRole);
                adminUser.setRoles(adminRolesList);
                
                userRepository.save(adminUser);
                log.info("Usuario administrador {} creado.", adminEmail);
            } else {
                log.info("Usuario administrador {} ya existe.", adminEmail);
            }
            
            log.info("Inicialización de base de datos completada.");
        };
    }
}