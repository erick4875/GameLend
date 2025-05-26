package org.project.group5.gamelend.repository;

import java.util.List;
import java.util.Optional;

import org.project.group5.gamelend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con usuarios
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de email
     * 
     * @param email dirección de correo electrónico
     * @return Optional con el usuario encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su nombre público
     * 
     * @param publicName nombre público del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<User> findByPublicName(String publicName);

    /**
     * Busca usuarios cuyo nombre público contiene el texto indicado (insensible a
     * mayúsculas/minúsculas)
     * 
     * @param text texto a buscar en el nombre público
     * @return Lista de usuarios que coinciden con la búsqueda
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.publicName) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<User> findByPublicNameContainingIgnoreCase(@Param("text") String text);

    /**
     * Verifica si existe un usuario con el nombre público dado
     * 
     * @param publicName Nombre público a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByPublicName(String publicName);

    /**
     * Verifica si existe un usuario con el email dado
     * 
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    
    boolean existsByPublicNameAndIdNot(String publicName, Long id);

    /**
     * Verifica si existe un usuario con el email dado, excluyendo un ID específico
     * (para evitar conflictos al actualizar un usuario)
     * 
     * @param email Email a verificar
     * @param id    ID del usuario a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca usuarios que tengan juegos asociados
     * 
     * @return Lista de usuarios con juegos
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.games")
    List<User> findAllWithGames();

    /**
     * Busca usuarios de una determinada ciudad
     * 
     * @param city nombre de la ciudad
     * @return Lista de usuarios de la ciudad
     */
    List<User> findByCity(String city);

    /**
     * Busca un usuario por su ID y carga todas sus relaciones
     * 
     * @param id ID del usuario
     * @return Usuario con sus relaciones cargadas
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.games WHERE u.id = :id")
    Optional<User> findByIdWithRelations(@Param("id") Long id);

    /**
     * Busca usuarios por el nombre de su rol
     * 
     * @param roleName nombre del rol
     * @return Lista de usuarios con el rol indicado
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Busca todos los usuarios con sus roles cargados
     * 
     * @return Lista de usuarios con roles
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();
}
