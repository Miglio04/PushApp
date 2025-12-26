package com.example.pushapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Training implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String description;
    private List<TrainingDay> trainingDaysList;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Costruttore vuoto richiesto da Firebase
    public Training() {
        this.trainingDaysList = new ArrayList<>();
    }

    public Training(String name, String description) {
        this.name = name;
        this.description = description;
        this.trainingDaysList = new ArrayList<>();
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Training(String name, String description, ArrayList<TrainingDay> trainingDaysList) {
        this.name = name;
        this.description = description;
        this.trainingDaysList = trainingDaysList != null ? trainingDaysList : new ArrayList<>();
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TrainingDay> getTrainingDaysList() { return trainingDaysList; }
    public void setTrainingDaysList(ArrayList<TrainingDay> trainingDaysList) {
        this.trainingDaysList = trainingDaysList;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public void addTrainingDay(TrainingDay day) {
        this.trainingDaysList.add(day);
    }

    public int getTotalDays() {
        return trainingDaysList.size();
    }
}
