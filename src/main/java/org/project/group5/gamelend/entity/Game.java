package org.project.group5.gamelend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un juego
 * Puede ser una plantilla de juego del catálogo general o una instancia
 * específica propiedad de un usuario
 */
@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    /**
     * Define los posibles estados de disponibilidad de un juego
     */
    public enum GameStatus {
        /** El juego está disponible para ser prestado. */
        AVAILABLE,
        /** El juego está actualmente prestado */
        BORROWED,
        /** El juego no está disponible por otras razones */
        UNAVAILABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 50)
    private String platform;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private GameStatus status;

    @Column(length = 50)
    private String genre;

    /** Documento (imagen) asociado al juego */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Document image;

    /**
     * Usuario propietario de esta instancia del juego (nulo si es un juego del
     * catálogo)
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Indica si es una plantilla del catálogo (true) o una instancia de usuario
     * (false)
     */
    @Column(name = "is_catalog", nullable = false)
    @Builder.Default
    private boolean catalog = false;

    /**
     * Si es un juego de usuario, referencia al juego del catálogo base (opcional)
     */
    @ManyToOne
    @JoinColumn(name = "catalog_game_id")
    private Game catalogGame;

    /** Historial de préstamos de este juego (si es una instancia de usuario) */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    /**
     * Si es un juego de catálogo, lista las instancias de este juego que poseen los
     * usuarios
     */
    @OneToMany(mappedBy = "catalogGame")
    @Builder.Default
    private List<Game> userGames = new ArrayList<>();

    /**
     * Constructor para juegos de usuario
     */
    public Game(long id, String title, String platform, String genre, String description,
            GameStatus status, User user, List<Loan> loans, Document image) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.status = status;
        this.user = user;
        this.image = image;
        this.loans = loans != null ? loans : new ArrayList<>();
        this.catalog = false; // Un juego con propietario y estado específico no es de catálogo
    }

    /**
     * Constructor para juegos de catálogo (plantillas)
     */
    public Game(String title, String platform, String genre, String description, Document image) {
        this.title = title;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.image = image;
        this.user = null;
        this.catalog = true;
        this.status = GameStatus.AVAILABLE;
    }

    /**
     * Constructor para crear una instancia de juego para un usuario, basada en un
     * juego del catálogo.
     */
    public Game(Game catalogGame, User user) {
        if (catalogGame == null || !catalogGame.isCatalog()) {
            throw new IllegalArgumentException("El juego base debe ser un juego de catálogo válido.");
        }
        if (user == null) {
            throw new IllegalArgumentException("Se requiere un usuario para crear una instancia de juego.");
        }
        this.title = catalogGame.getTitle();
        this.platform = catalogGame.getPlatform();
        this.genre = catalogGame.getGenre();
        this.description = catalogGame.getDescription();
        this.image = catalogGame.getImage();
        this.catalogGame = catalogGame;
        this.user = user;
        this.status = GameStatus.AVAILABLE;
        this.catalog = false;
    }

    /**
     * Verifica si esta instancia es un juego de catálogo.
     * 
     * @return true si es un juego de catálogo, false en caso contrario.
     */
    public boolean isCatalog() {
        return catalog;
    }

    /**
     * Verifica si es un juego de un usuario vinculado a una plantilla de catálogo.
     * 
     * @return true si es un juego de usuario vinculado, false en caso contrario.
     */
    public boolean isUserGameLinkedToCatalog() {
        return !this.catalog && this.user != null && this.catalogGame != null;
    }


}
