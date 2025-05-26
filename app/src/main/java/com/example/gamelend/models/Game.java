package com.example.gamelend.models;

public class Game {
    private String name;
    private int imageResourceId;
    private Long id;

    public Game(Long id, String name, int imageResourceId) {
        this.id = id;
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    public Long getId() { return id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
}

