package com.example.pushapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TrainingDay implements Serializable {
    @PrimaryKey
    private String id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "dayOrder")
    private int dayOrder;
    @Ignore
    private List<Exercise> exercises;
    @ColumnInfo(name = "notes")
    private String notes;

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
    public TrainingDay(String name, int dayOrder, ArrayList<Exercise> exercises) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dayOrder = dayOrder;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) {
        if (id != null) {
            this.id = id;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDayOrder() { return dayOrder; }
    public void setDayOrder(int dayOrder) { this.dayOrder = dayOrder; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
    }

    public int getTotalExercises() {
        return exercises.size();
    }
}
