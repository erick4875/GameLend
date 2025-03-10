package org.project.group5.gamelend.repository;

import java.util.List;

import org.project.group5.gamelend.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para operaciones de base de datos relacionadas con préstamos de juegos
 */
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /**
     * Encuentra préstamos donde el usuario indicado es el prestador
     * @param usuarioId ID del usuario prestador
     * @return Lista de préstamos realizados por el usuario
     */
    @Query("SELECT p FROM Prestamo p WHERE p.usuario.id = :usuarioId")
    List<Prestamo> findByUsuariId(@Param("usuarioId") Long usuarioId);

    /**
     * Encuentra préstamos donde el usuario indicado es el receptor
     * @param usuarioId ID del usuario receptor
     * @return Lista de préstamos recibidos por el usuario
     */
    @Query("SELECT p FROM Prestamo p WHERE p.usuarioReceptor.id = :usuarioId")
    List<Prestamo> findByUsuarioReceptorId(@Param("usuarioId") Long usuarioId);

    /**
     * Encuentra préstamos activos (sin fecha de devolución)
     * @return Lista de préstamos activos
     */
    @Query("SELECT p FROM Prestamo p WHERE p.fechaDevolucion IS NULL")
    List<Prestamo> findPrestamosActivos();

    /**
     * Encuentra préstamos activos de un juego específico
     * @param juegoId ID del juego
     * @return Lista de préstamos activos del juego
     */
    @Query("SELECT p FROM Prestamo p WHERE p.juego.id = :juegoId AND p.fechaDevolucion IS NULL")
    List<Prestamo> findPrestamosActivosByJuegoId(@Param("juegoId") Long juegoId);

    /**
     * Verifica si un juego está actualmente prestado
     * @param juegoId ID del juego
     * @return true si el juego está prestado, false en caso contrario
     */
    @Query("SELECT COUNT(p) > 0 FROM Prestamo p WHERE p.juego.id = :juegoId AND p.fechaDevolucion IS NULL")
    boolean isJuegoPrestado(@Param("juegoId") Long juegoId);
}
