package org.project.group5.gamelend.config;

import java.time.LocalDateTime;
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
 * Inicializador de la base de datos.
 * Crea roles y usuario admin por defecto al arrancar la aplicación.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inicializa la base de datos con datos básicos.
     * Se ejecuta automáticamente al iniciar la aplicación.
     */
    @Bean
    @Order(1)
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("Inicializando roles y usuario administrador...");

            // Crear roles básicos
            Role userRole = createRoleIfNotExists("ROLE_USER");
            Role adminRole = createRoleIfNotExists("ROLE_ADMIN");

            // Crear admin por defecto
            createDefaultAdmin(userRole, adminRole);

            log.info("Inicialización completada.");
        };
    }

    /**
     * Crea un rol si no existe.
     */
    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            log.info("Creando rol {}", roleName);
            return roleRepository.save(newRole);
        });
    }

    /**
     * Crea el usuario administrador por defecto si no existe.
     */
    private void createDefaultAdmin(Role userRole, Role adminRole) {
        String adminEmail = "admin@linkiafp.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creando usuario administrador: {}", adminEmail);

            User adminUser = new User();
            adminUser.setName("Administrador LinkiaFP");
            adminUser.setPublicName("linkiaFPAdmin");
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode("linkiaFP"));
            adminUser.setProvince("Barcelona");
            adminUser.setCity("Badalona");
            adminUser.setRegistrationDate(LocalDateTime.now());

            // Asignar roles
            adminUser.setRoles(List.of(adminRole, userRole));

            userRepository.save(adminUser);
            log.info("Usuario administrador creado: {}", adminEmail);
        } else {
            log.info("Usuario administrador ya existe: {}", adminEmail);
        }
    }
}