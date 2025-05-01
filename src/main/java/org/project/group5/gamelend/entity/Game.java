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

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    /**
     * Estados posibles para un juego
     */
    public enum GameStatus {
        AVAILABLE, // Disponible para préstamo
        BORROWED,  // Prestado actualmente
        UNAVAILABLE // No disponible (perdido, dañado, etc.)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 50)
    private String platform;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status;

    @Column(length = 30)
    private String genre;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans;

    private String imagePath;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Document image;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_catalog", nullable = false)
    @Builder.Default
    private boolean isCatalog = false;

    @ManyToOne
    @JoinColumn(name = "catalog_game_id")
    private Game catalogGame;

    @OneToMany(mappedBy = "catalogGame")
    private List<Game> userGames;

    // Constructor for loans
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
    }

    // Constructor for catalog games
    public Game(String title, String platform, String genre, String description, Document image) {
        this.title = title;
        this.platform = platform;
        this.genre = genre;
        this.description = description;
        this.image = image;
        this.user = null;
        this.catalogGame = null;
        this.userGames = new ArrayList<>();
        this.loans = new ArrayList<>();
    }

    // Constructor for user games based on catalog game
    public Game(Game catalogGame, User user) {
        this.title = catalogGame.getTitle();
        this.platform = catalogGame.getPlatform();
        this.genre = catalogGame.getGenre();
        this.description = catalogGame.getDescription();
        this.image = catalogGame.getImage();
        this.catalogGame = catalogGame;
        this.user = user;
        this.status = GameStatus.AVAILABLE;
        this.loans = new ArrayList<>();
    }

    // Custom methods
    public boolean isCatalog() {
        return isCatalog || (user == null && catalogGame == null);
    }

    public boolean isUserGame() {
        return catalogGame != null && user != null;
    }
}

