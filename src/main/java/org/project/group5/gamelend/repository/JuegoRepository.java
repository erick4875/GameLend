package org.project.group5.gamelend.repository;

import java.util.List;
import java.util.Optional;

import org.project.group5.gamelend.entity.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con juegos
 */
public interface JuegoRepository extends JpaRepository<Juego, Long> {

    /**
     * Busca un juego por su nombre
     * @param titulo titulo del juego
     * @return Optional con el juego encontrado
     */
    Optional<Juego> findByTitulo(String titulo);

    /**
     * Busca un juego por la ruta de su imagen
     * @param imagenRuta ruta de la imagen del juego
     * @return Optional con el juego encontrado
     */
    Optional<Juego> findByImagenRuta(String imagenRuta);

    /**
     * Busca los juegos de un usuario específico que tienen imagen asociada
     * @param usuarioId ID del usuario propietario
     * @return Lista de juegos con imágenes del usuario
     */
    @Query("SELECT j FROM Juego j WHERE j.usuario.id = :usuarioId AND j.imagenRuta IS NOT NULL")
    List<Juego> findByUsuarioIdAndImagenRutaNotNull(@Param("usuarioId") Long usuarioId);

    /**
     * Encuentra juegos de un usuario que tengan imágenes asociadas
     */
    List<Juego> findByUsuarioIdAndImagenIsNotNull(Long usuarioId);

    /**
     * Busca todos los juegos de un usuario
     * @param usuarioId ID del usuario propietario
     * @return Lista de todos los juegos del usuario
     */
    @Query("SELECT j FROM Juego j WHERE j.usuario.id = :usuarioId")
    List<Juego> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca juegos cuyo nombre contiene el texto indicado (búsqueda insensible a mayúsculas/minúsculas)
     * @param texto texto a buscar en el nombre del juego
     * @return Lista de juegos que coinciden con la búsqueda
     */
    @Query("SELECT j FROM Juego j WHERE LOWER(j.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Juego> findByNombreContainingIgnoreCase(@Param("texto") String texto);

    /**
     * Encuentra juegos que no están actualmente prestados
     * @return Lista de juegos disponibles
     */
    @Query("SELECT j FROM Juego j WHERE j.id NOT IN (SELECT p.juego.id FROM Prestamo p WHERE p.fechaDevolucion IS NULL)")
    List<Juego> findJuegosDisponibles();
}
