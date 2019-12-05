package com.example.musicsuggestor;

public class SongStatus {
    private int category;
    private float speed;
    private float volume;

    public SongStatus(int category, float speed, float volume) {
        this.category = category;
        this.speed = speed;
        this.volume = volume;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
