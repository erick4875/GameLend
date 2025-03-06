package org.project.group5.gamelend.repository;

import java.util.List;
import java.util.Optional;

import org.project.group5.gamelend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con usuarios
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su dirección de email
     * 
     * @param email dirección de correo electrónico
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su nombre público
     * 
     * @param nombrePublico nombre público del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByNombrePublico(String nombrePublico);

    /**
     * Busca usuarios cuyo nombre público contiene el texto indicado (insensible a
     * mayúsculas/minúsculas)
     * 
     * @param texto texto a buscar en el nombre público
     * @return Lista de usuarios que coinciden con la búsqueda
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombrePublico) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Usuario> findByNombrePublicoContainingIgnoreCase(@Param("texto") String texto);



    /**
     * Verifica si existe un usuario con el nombre público dado
     * 
     * @param nombrePublico Nombre público a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombrePublico(String nombrePublico);

    /**
     * Verifica si existe un usuario con el email dado
     * 
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios que tengan juegos asociados
     * 
     * @return Lista de usuarios con juegos
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.juegos")
    List<Usuario> findAllWithJuegos();

    /**
     * Busca usuarios de una determinada localidad
     * 
     * @param localidad nombre de la localidad
     * @return Lista de usuarios de la localidad
     */
    List<Usuario> findByLocalidad(String localidad);

    /**
     * Busca un usuario por su ID y carga todas sus relaciones
     * 
     * @param id ID del usuario
     * @return Usuario con sus relaciones cargadas
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.juegos WHERE u.id = :id")
    Optional<Usuario> findByIdWithRelaciones(@Param("id") Long id);

    /**
     * Busca usuarios por el nombre de su rol
     * 
     * @param rolName nombre del rol
     * @return Lista de usuarios con el rol indicado
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE r.name = :rolName")
    List<Usuario> findByRolName(@Param("rolName") String rolName);
}
