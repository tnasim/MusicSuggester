package com.example.musicsuggestor;

public class SongStatus {
    private int category;
    private double speed;
    private double volume;

    public SongStatus(int category, double speed, double volume) {
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
