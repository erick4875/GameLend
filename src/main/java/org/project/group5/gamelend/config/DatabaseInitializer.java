package org.project.group5.gamelend.config;

import org.project.group5.gamelend.entity.Role;
import org.project.group5.gamelend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final RoleRepository roleRepository;

    @Bean
    @Order(1) // Asegura que este inicializador corra primero
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Inicializando roles en la base de datos");
            
            if (!roleRepository.existsByName("ROLE_USER")) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                roleRepository.save(userRole);
                log.info("Rol ROLE_USER creado");
            }
            
            if (!roleRepository.existsByName("ROLE_ADMIN")) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);
                log.info("Rol ROLE_ADMIN creado");
            }
            
            log.info("Inicialización de roles completada");
        };
    }
}