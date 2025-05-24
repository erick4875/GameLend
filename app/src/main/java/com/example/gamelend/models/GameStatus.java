package com.example.gamelend.models;

// AVAILABLE disponible
// BORROWED prestado
// UNAVAILABLE no disponible por otras razones (ej. extraviado)
public enum GameStatus {
    AVAILABLE,
    BORROWED,
    UNAVAILABLE;

    public static GameStatus fromString(String value) {
        if (value == null) return null;
        for (GameStatus status : GameStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
