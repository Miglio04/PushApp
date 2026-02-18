package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

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
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    @ColumnInfo(name = "updatedAt")
    private long updatedAt;
    @ColumnInfo(name = "deleted")
    private boolean deleted;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "trainingId")
    private String trainingId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "notes")
    private String notes;
    @Ignore
    private List<WorkoutExercise> workoutExercises;

    public Routine() {
        this.routineId = UUID.randomUUID().toString();
        this.workoutExercises = new ArrayList<>();
        this.deleted = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Routine(String name) {
        this.routineId = UUID.randomUUID().toString();
        this.name = name;
        this.workoutExercises = new ArrayList<>();
        this.deleted = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public Routine(Routine routine) {
        this.trainingId = routine.getTrainingId();
        this.userId = routine.getUserId();
        this.routineId = routine.getRoutineId();
        this.name = routine.getName();
        if (routine.getWorkoutExercises() != null) {
            this.workoutExercises = new ArrayList<>(routine.getWorkoutExercises());
        } else {
            this.workoutExercises = new ArrayList<>();
        }
        this.deleted = routine.isDeleted();
        this.createdAt = routine.getCreatedAt();
        this.updatedAt = routine.getUpdatedAt();
        this.notes = routine.getNotes();
    }

    // Getters e Setters
    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { if (routineId != null) { this.routineId = routineId; } }

    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Exclude
    @Ignore
    public List<WorkoutExercise> getWorkoutExercises() {
        if (workoutExercises == null) workoutExercises = new ArrayList<>();
        return workoutExercises;
    }
    @Ignore
    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) { this.workoutExercises = workoutExercises; }
    @Ignore
    public void addWorkoutExercise(WorkoutExercise workoutExercise) { this.workoutExercises.add(workoutExercise); }
    @Exclude
    @Ignore
    public int getWorkoutTotalExercises() {
        return workoutExercises.size();
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
