package com.example.pushapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "training")
public class Training implements Serializable {
    @PrimaryKey
    private String id;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "isActive")
    private boolean isActive;
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    @ColumnInfo(name = "updatedAt")
    private long updatedAt;
    @Ignore
    private List<TrainingDay> trainingDaysList;

    // Costruttore vuoto richiesto da Firebase
    public Training() {
        this.trainingDaysList = new ArrayList<>();
    }

    @Ignore
    public Training(String name, String description) {
        this.name = name;
        this.description = description;
        this.trainingDaysList = new ArrayList<>();
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
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
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    @Ignore
    public List<TrainingDay> getTrainingDaysList() { return trainingDaysList; }
    @Ignore
    public void setTrainingDaysList(ArrayList<TrainingDay> trainingDaysList) {
        this.trainingDaysList = trainingDaysList;
    }
    @Ignore
    public void addTrainingDay(TrainingDay day) {
        this.trainingDaysList.add(day);
    }
    @Ignore
    public int getTotalDays() {
        return trainingDaysList.size();
    }
}
