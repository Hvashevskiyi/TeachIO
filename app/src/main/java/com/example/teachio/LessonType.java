package com.example.teachio;

public class LessonType {
    private int id;
    private String name;

    public LessonType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
