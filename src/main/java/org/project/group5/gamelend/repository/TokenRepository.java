package org.project.group5.gamelend.repository;

import java.util.List;
import java.util.Optional;

import org.project.group5.gamelend.entity.Token;
import org.project.group5.gamelend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con tokens
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    /**
     * Encuentra todos los tokens válidos (no expirados y no revocados) para un usuario
     * 
     * @param userId ID del usuario
     * @return Lista de tokens válidos
     */
    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.expired = false AND t.revoked = false")
    List<Token> findAllValidTokensByUser(Long userId);
    
    /**
     * Encuentra todos los tokens válidos o semi-válidos para un usuario
     * (tokens donde expired=false O revoked=false)
     * 
     * @param userId ID del usuario
     * @return Lista de tokens válidos o parcialmente válidos
     */
    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND (t.expired = false OR t.revoked = false)")
    List<Token> findAllValidIsFalseOrRevokedIsFalseByUser(Long userId);
    
    /**
     * Encuentra un token por su valor
     * 
     * @param token Valor del token a buscar
     * @return Token encontrado (opcional)
     */
    Optional<Token> findByToken(String token);
    
    /**
     * Invalida todos los tokens de un usuario
     * 
     * @param user Usuario cuyos tokens serán invalidados
     */
    @Query("UPDATE Token t SET t.expired = true, t.revoked = true WHERE t.user = :user AND t.expired = false AND t.revoked = false")
    void invalidateAllUserTokens(User user);
    
    /**
     * Cuenta cuántos tokens válidos tiene un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tokens válidos
     */
    @Query("SELECT COUNT(t) FROM Token t WHERE t.user.id = :userId AND t.expired = false AND t.revoked = false")
    int countValidTokensByUser(Long userId);
}
