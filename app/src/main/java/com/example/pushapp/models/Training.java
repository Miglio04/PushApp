package com.example.pushapp.models;

import java.util.ArrayList;

public class Training {
    private int id;

    private String name;

    private String description;

    public ArrayList<Workout> getWorkoutList() {
        return workoutList;
    }

    public void setWorkoutList(ArrayList<Workout> workoutList) {
        this.workoutList = workoutList;
    }

    private ArrayList<Workout> workoutList;


    public Training(int id, String name, String description){
        setId(id);
        setName(name);
        setDescription(description);
    }

    public Training(int id, String name, String description, ArrayList<Workout> workoutList){
        setId(id);
        setName(name);
        setDescription(description);
        setWorkoutList(workoutList);
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
