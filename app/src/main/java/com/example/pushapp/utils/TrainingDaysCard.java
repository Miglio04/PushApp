package com.example.pushapp.utils;

public class TrainingDaysCard {
    private final String title;
    private final String description;
    private String trainingDayId;

    public TrainingDaysCard(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public TrainingDaysCard(String title, String description, String trainingDayId) {
        this.title = title;
        this.description = description;
        this.trainingDayId = trainingDayId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTrainingDayId(){ return trainingDayId; }
}

