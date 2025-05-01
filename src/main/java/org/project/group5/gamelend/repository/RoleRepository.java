package org.project.group5.gamelend.repository;

import java.util.Optional;

import org.project.group5.gamelend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca un rol por su nombre
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica si existe un rol con el nombre indicado
     */
    boolean existsByName(String name);

    /**
     * Asigna un rol a un usuario
     * @param userId ID del usuario
     * @param roleId ID del rol
     */
    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)", nativeQuery = true)
    void assignRoleToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * Elimina un rol de un usuario
     * @param userId ID del usuario
     * @param roleId ID del rol
     */
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    void removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
}