package org.project.group5.gamelend.repository;

import java.util.List;
import java.util.Optional;

import org.project.group5.gamelend.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para operaciones de base de datos relacionadas con juegos
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Encuentra un juego por su título ignorando mayúsculas/minúsculas
     * 
     * @param title título del juego
     * @return Optional con el juego o vacío si no existe
     */
    Optional<Game> findByTitleIgnoreCase(String title);

    /**
     * Busca un juego por la ruta de su imagen
     * 
     * @param imagePath ruta de la imagen del juego
     * @return Optional con el juego encontrado
     */
    Optional<Game> findByImage_FileName(String fileName);

    /**
     * Encuentra todos los juegos de un usuario específico
     * 
     * @param userId ID del usuario propietario
     * @return Lista de juegos del usuario
     */
    List<Game> findByUserId(Long userId);

    /**
     * Busca los juegos de un usuario específico que tienen imagen asociada
     * 
     * @param userId ID del usuario propietario
     * @return Lista de juegos con imágenes del usuario
     */
    @Query("SELECT g FROM Game g WHERE g.user.id = :userId AND g.image IS NOT NULL")
    List<Game> findByUserIdAndImageIsNotNull(@Param("userId") Long userId);

    /**
     * Busca juegos cuyo nombre contiene el texto indicado ignorando mayúsculas/minúsculas
     * 
     * @param text texto a buscar en el nombre del juego
     * @return Lista de juegos que coinciden con la búsqueda
     */
    @Query("SELECT g FROM Game g WHERE LOWER(g.title) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Game> findByTitleContainingIgnoreCase(@Param("text") String text);

    /**
     * Encuentra juegos que no están actualmente prestados
     * 
     * @return Lista de juegos disponibles
     */
    @Query("SELECT g FROM Game g WHERE g.id NOT IN (SELECT l.game.id FROM Loan l WHERE l.returnDate IS NULL)")
    List<Game> findAvailableGames();
}
