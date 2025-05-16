package com.example.gamelend.dto;

public enum GameStatus {
    AVAILABLE,
    BORROWED,
    UNAVAILABLE;

    public static GameStatus fromString(String value) {
        for (GameStatus status : GameStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
