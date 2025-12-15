package com.example.pushapp.utils;

public class WorkoutSet {
    private float weight;
    private int reps;
    private boolean completed;


    public WorkoutSet(float weight, int reps) {
        this.weight = weight;
        this.reps = reps;
        this.completed = false;
    }

    public float getWeight() { return weight; }
    public int getReps() { return reps; }
    public boolean getCompleted() { return completed; }
    public void setWeight(float weight) { this.weight = weight; }
    public void setReps(int reps) { this.reps = reps; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}

