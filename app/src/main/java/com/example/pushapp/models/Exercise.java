package com.example.pushapp.models;

public class Exercise {
    private String name;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Exercise(String name, int id) {
        setName(name);
        setId(id);
    }
}
