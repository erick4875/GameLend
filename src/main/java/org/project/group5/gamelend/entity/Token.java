package org.project.group5.gamelend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    public enum TokenType {
        BEARER
    }

    // Identificador único del token
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token que se almacena en la base de datos
    // multiples tokens pueden ser generados para el mismo usuario
    // puede estar logueado en varios dispositivos
    @Column(unique = true, nullable = false, length = 500)
    private String token;

    // Tipo de token, por defecto es BEARER
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    @Builder.Default
    private TokenType tokenType = TokenType.BEARER;

    // Estado del token
    @Column(nullable = false)
    private boolean revoked;
    
    @Column(nullable = false)
    private boolean expired;
    
    // Fecha de creación para control de tokens antiguos
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Referencia al usuario al que pertenece el token
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}