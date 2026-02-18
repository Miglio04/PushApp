package com.example.pushapp.models;

public class WorkoutSet {
    private float weight;
    private int reps;


    public WorkoutSet(float weight, int reps) {
        this.weight = weight;
        this.reps = reps;
    }

    public float getWeight() { return weight; }
    public int getReps() { return reps; }

    public void setWeight(float weight) { this.weight = weight; }
    public void setReps(int reps) { this.reps = reps; }
    
}

