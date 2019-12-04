package com.example.musicsuggestor;


public enum SongCategory
{
    rest(1),
    walk(2),
    workout(3),
    study(4),
    driving(5),
    other(999);

    private int category;

    SongCategory(int category) {
        this.category = category;
    }

    public int getValue() {
        return category;
    }

    public SongCategory getCategory(String catName) {
        switch (catName) {
            case "walk":
                return walk;
            case "rest":
                return rest;
            case "workout":
                return workout;
            case "study":
                return study;
            case "driving":
                return driving;
        }
        return other;
    }
}
