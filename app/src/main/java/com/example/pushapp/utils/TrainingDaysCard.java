package com.example.pushapp.utils;

public class TrainingDaysCard {
    private final String title;
    private final String description;
    private int trainingDayId;

    public TrainingDaysCard(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public TrainingDaysCard(String title, String description, int id) {
        this.title = title;
        this.description = description;
        this.trainingDayId = id;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTrainingDayId(){ return trainingDayId; }
}

