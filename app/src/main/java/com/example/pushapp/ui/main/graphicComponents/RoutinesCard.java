package com.example.pushapp.ui.main.graphicComponents;

public class RoutinesCard {
    private final String title;
    private final String description;
    private String trainingDayId;

    public RoutinesCard(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public RoutinesCard(String title, String description, String trainingDayId) {
        this.title = title;
        this.description = description;
        this.trainingDayId = trainingDayId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTrainingDayId(){ return trainingDayId; }
}

