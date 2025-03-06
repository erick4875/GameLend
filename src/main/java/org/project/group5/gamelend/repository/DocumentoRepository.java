package org.project.group5.gamelend.repository;

import jakarta.transaction.Transactional;
import org.project.group5.gamelend.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con documentos/imágenes
 */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    /**
     * Lista todos los documentos activos que no están marcados como eliminados
     * @return Iterable de documentos activos
     */
    @Query("SELECT d FROM Documento d WHERE d.estado = 'D' AND d.eliminado = false")
    Iterable<Documento> list();
    
    /**
     * Busca un documento por su nombre de archivo, estado y estado de eliminación
     * @param fileName nombre del archivo
     * @param estado estado del documento ('D' para disponible)
     * @param eliminado indica si el documento está marcado como eliminado
     * @return Optional con el documento encontrado
     */
    @Query("SELECT d FROM Documento d WHERE d.fileName = :fileName AND d.estado = :estado AND d.eliminado = :eliminado")
    Optional<Documento> findByFileNameAndStateAndDeleted(
            @Param("fileName") String fileName, 
            @Param("estado") String estado, 
            @Param("eliminado") boolean eliminado);

    /**
     * Elimina un documento por su ID
     * @param id ID del documento a eliminar
     * @return número de registros eliminados
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Documento d WHERE d.id = :id")
    int deleteImagenById(@Param("id") Long id);

    /**
     * Busca un documento por su nombre de archivo
     * @param fileName nombre del archivo
     * @return el documento encontrado o null
     */
    Documento findByFileName(String fileName);
}
