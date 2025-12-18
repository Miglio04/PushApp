package com.example.pushapp.models;

public class Workout {
    private int id;
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Workout(int id, String name){
        setId(id);
        setName(name);
    }
}
