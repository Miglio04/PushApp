package com.example.pushapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "training_day",
        foreignKeys = @ForeignKey(
                entity = Training.class,
                parentColumns = "id",
                childColumns = "training_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "training_id")})
public class TrainingDay implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "training_id")
    private String trainingId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "dayOrder")
    private int dayOrder;
    @ColumnInfo(name = "notes")
    private String notes;
    @Ignore
    private List<Exercise> exercises;

    // Costruttore vuoto richiesto da Firebase
    public TrainingDay() {
        this.id = UUID.randomUUID().toString();
        this.exercises = new ArrayList<>();
    }

    public TrainingDay(String name, int dayOrder) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dayOrder = dayOrder;
        this.exercises = new ArrayList<>();
    }

    // Ha senso questo costruttore?
    @Ignore
    public TrainingDay(String name, int dayOrder, ArrayList<Exercise> exercises) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dayOrder = dayOrder;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { if (id != null) { this.id = id; } }
    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDayOrder() { return dayOrder; }
    public void setDayOrder(int dayOrder) { this.dayOrder = dayOrder; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Ignore
    public List<Exercise> getExercises() {
        if (exercises == null) exercises = new ArrayList<>();
        return exercises;
    }
    @Ignore
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
    @Ignore
    public void addExercise(Exercise exercise) { this.exercises.add(exercise); }
    @Ignore
    public int getTotalExercises() {
        return exercises.size();
    }
}
