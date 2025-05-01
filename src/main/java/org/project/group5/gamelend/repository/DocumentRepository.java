package org.project.group5.gamelend.repository;

import java.util.Optional;

import org.project.group5.gamelend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

/**
 * Repositorio para operaciones de base de datos relacionadas con
 * documentos/imágenes
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Lista todos los documentos activos que no están marcados como eliminados
     * 
     * @return Iterable de documentos activos
     */
    @Query("SELECT d FROM Document d WHERE d.status = 'D' AND d.deleted = false")
    Iterable<Document> list();

    /**
     * Busca un documento por su nombre de archivo, estado y estado de eliminación
     * 
     * @param fileName nombre del archivo
     * @param status   estado del documento ('D' para disponible)
     * @param deleted  indica si el documento está marcado como eliminado
     * @return Optional con el documento encontrado
     */
    @Query("SELECT d FROM Document d WHERE d.fileName = :fileName AND d.status = :status AND d.deleted = :deleted")
    Optional<Document> findByFileNameAndStatusAndDeleted(
            @Param("fileName") String fileName,
            @Param("status") String status,
            @Param("deleted") boolean deleted);

    /**
     * Elimina un documento por su ID
     * 
     * @param id ID del documento a eliminar
     * @return número de registros eliminados
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Document d WHERE d.id = :id")
    int deleteDocumentById(@Param("id") Long id);

    /**
     * Busca un documento por su nombre de archivo
     * 
     * @param fileName nombre del archivo
     * @return el documento encontrado o null
     */
    Document findByFileName(String fileName);
}
