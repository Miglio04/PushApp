package com.example.pushapp.models;

import java.util.ArrayList;

public class TrainingDay {
    private int id;
    private String name;

    public ArrayList<ExerciseSeries> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<ExerciseSeries> exercises) {
        this.exercises = exercises;
    }

    private ArrayList<ExerciseSeries> exercises;


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

    public TrainingDay(int id, String name, ArrayList<ExerciseSeries> exercises){
        setId(id);
        setName(name);
        setExercises(exercises);
    }

    public TrainingDay(int id, String name){
        setId(id);
        setName(name);
    }
}
