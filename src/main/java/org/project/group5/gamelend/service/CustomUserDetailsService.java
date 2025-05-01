package org.project.group5.gamelend.service;

import java.util.List;

import org.project.group5.gamelend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Servicio para cargar los detalles del usuario en Spring Security
// Este servicio se encarga de cargar los detalles del usuario desde la base de datos
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        /**
         * Carga un usuario por su nombre de usuario (email o nombre público)
         * 
         * @param username email o nombre público del usuario
         * @return Detalles del usuario para Spring Security
         * @throws UsernameNotFoundException si el usuario no existe
         */
        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                log.debug("Cargando usuario por nombre: {}", username);

                // Primero busca por email
                org.project.group5.gamelend.entity.User user = userRepository.findByEmail(username)
                                // Si no se encuentra por email, busca por nombre público
                                .orElseGet(() -> userRepository.findByPublicName(username)
                                                .orElseThrow(() -> new UsernameNotFoundException(
                                                                "Usuario no encontrado con email o nombre: "
                                                                                + username)));

                // Convertir los roles a authorities
                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName())) // "ROLE_ADMIN", "ROLE_USER"
                                .toList();

                log.debug("Usuario encontrado: {} con {} roles", user.getEmail(), authorities.size());

                // Construye y retorna el UserDetails
                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                authorities);
        }
}
