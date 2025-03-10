package org.project.group5.gamelend.service;

import java.util.ArrayList;
import java.util.List;

import org.project.group5.gamelend.entity.Usuario;
import org.project.group5.gamelend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio personalizado para cargar detalles de usuarios desde la base de datos
 * para la autenticación y autorización de Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su nombre de usuario (email o nombre público)
     * @param username email o nombre público del usuario
     * @return Detalles del usuario para Spring Security
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Primero busca por email
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseGet(() -> usuarioRepository.findByNombrePublico(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "Usuario no encontrado con email o nombre: " + username)));
        
        // Crear autoridades basadas en el campo isAdmin
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (usuario.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Todos son al menos usuarios
        
        // Construye y retorna el UserDetails
        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }
}
