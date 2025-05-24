package com.example.gamelend.models; // Asegúrate que el paquete sea 'models'

public class Game {
    private String name; // Antes 'nombre'
    private int imageResourceId; // Antes 'imagenResource'

    // Puedes añadir más campos aquí si los necesitas para la UI,
    // que podrían venir mapeados desde GameSummaryDTO o GameResponseDTO.
    // Por ejemplo:
    // private String platform;
    // private GameStatus status; // Usaría tu enum com.example.gamelend.models.GameStatus
    // private String imageUrl; // Si vas a cargar imágenes desde una URL con Glide

    public Game(String name, int imageResourceId) {
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    // Constructor adicional si quieres más campos para la UI
    /*
    public Game(String name, String imageUrl, String platform, GameStatus status) {
        this.name = name;
        this.imageUrl = imageUrl; // Si usas URL en lugar de resource ID
        this.platform = platform;
        this.status = status;
    }
    */

    public String getName() { // Antes getNombre
        return name;
    }

    public void setName(String name) { // Antes setNombre
        this.name = name;
    }

    public int getImageResourceId() { // Antes getImagenResource
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) { // Antes setImagenResource
        this.imageResourceId = imageResourceId;
    }

    // Getters y Setters para campos adicionales si los añades
    /*
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    */
}

