package com.example.pushapp.models;

import androidx.annotation.NonNull;
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

@Entity(tableName = "routine",
        foreignKeys = @ForeignKey(
                entity = Training.class,
                parentColumns = "trainingId",
                childColumns = "trainingId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "trainingId")})
public class Routine implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "routineId")
    @NonNull
    private String routineId;
    @ColumnInfo(name = "trainingId")
    private String trainingId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "dayOrder")
    private int dayOrder;
    @ColumnInfo(name = "notes")
    private String notes;
    @Ignore
    private List<WorkoutExercise> workoutExercises;

    // Costruttore vuoto richiesto da Firebase
    public Routine() {
        this.routineId = UUID.randomUUID().toString();
        this.workoutExercises = new ArrayList<>();
    }

    public Routine(String name, int dayOrder) {
        this.routineId = UUID.randomUUID().toString();
        this.name = name;
        this.dayOrder = dayOrder;
        this.workoutExercises = new ArrayList<>();
    }

    // Ha senso questo costruttore?
    @Ignore
    public Routine(String name, int dayOrder, ArrayList<WorkoutExercise> workoutExercises) {
        this.routineId = UUID.randomUUID().toString();
        this.name = name;
        this.dayOrder = dayOrder;
        this.workoutExercises = workoutExercises != null ? workoutExercises : new ArrayList<>();
    }

    // Getters e Setters
    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { if (routineId != null) { this.routineId = routineId; } }
    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDayOrder() { return dayOrder; }
    public void setDayOrder(int dayOrder) { this.dayOrder = dayOrder; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Ignore
    public List<WorkoutExercise> getWorkoutExercises() {
        if (workoutExercises == null) workoutExercises = new ArrayList<>();
        return workoutExercises;
    }
    @Ignore
    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) { this.workoutExercises = workoutExercises; }
    @Ignore
    public void addWorkoutExercise(WorkoutExercise workoutExercise) { this.workoutExercises.add(workoutExercise); }
    @Ignore
    public int getWorkoutTotalExercises() {
        return workoutExercises.size();
    }
}
