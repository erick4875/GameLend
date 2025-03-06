package org.project.group5.gamelend.repository;

import java.util.Optional;

import org.project.group5.gamelend.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con roles de usuario
 */
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {
    
    /**
     * Busca un rol por su nombre
     * @param name nombre del rol
     * @return Optional con el rol encontrado
     */
    Optional<UsuarioRol> findByName(String name);
    
    /**
     * Verifica si existe un rol con el nombre indicado
     * @param name nombre del rol
     * @return true si existe, false en caso contrario
     */
    boolean existsByName(String name);

    @Modifying
    @Query(value = "INSERT INTO usuario_rol_asignacion (usuario_id, rol_id) VALUES (:usuarioId, :rolId)", nativeQuery = true)
    void asignarRol(@Param("usuarioId") Long usuarioId, @Param("rolId") Long rolId);
}
