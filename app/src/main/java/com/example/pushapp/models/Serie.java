package com.example.pushapp.models;

public class Serie {
    private int serieNumber;
    private int targetReps;
    private double targetWeight;
    private int actualReps;
    private double actualWeight;
    private boolean completed;

    public Serie() {}

    public Serie(int serieNumber, int targetReps, double targetWeight) {
        this.serieNumber = serieNumber;
        this.targetReps = targetReps;
        this.targetWeight = targetWeight;
        this.completed = false;
    }

    // Getters e Setters
    public int getSerieNumber() { return serieNumber; }
    public void setSerieNumber(int serieNumber) { this.serieNumber = serieNumber; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public int getActualReps() { return actualReps; }
    public void setActualReps(int actualReps) { this.actualReps = actualReps; }

    public double getActualWeight() { return actualWeight; }
    public void setActualWeight(double actualWeight) { this.actualWeight = actualWeight; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
