package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "workoutExercise",
        foreignKeys = @ForeignKey(
                entity = Routine.class,
                parentColumns = "routineId",
                childColumns = "routineId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("routineId"))

public class WorkoutExercise implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "workoutExerciseId")
    @NonNull
    private String workoutExerciseId;
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    @ColumnInfo(name = "updatedAt")
    private long updatedAt;
    @ColumnInfo(name = "deleted")
    private boolean deleted;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "routineId")
    private String routineId;
    @ColumnInfo(name = "apiExerciseId")
    private String apiExerciseId;
    @ColumnInfo(name = "exerciseOrder")
    private int order;
    @ColumnInfo(name = "restTimeIndex")
    private int restTimeIndex = 2;  // Default index (90s)
    @Ignore
    private List<Serie> series;
    @Ignore
    @Exclude
    private boolean isExpanded = false;
    @Ignore
    @Exclude
    private String muscleGroup;

    // Costruttore vuoto per Firebase
    public WorkoutExercise() {
        this.workoutExerciseId = UUID.randomUUID().toString();
        this.series = new ArrayList<>();
    }

    // Costruttore per creare un nuovo esercizio a partire da un esercizio base dell'API
    public WorkoutExercise(String apiExerciseId, int order) {
        this.workoutExerciseId = UUID.randomUUID().toString();
        this.apiExerciseId = apiExerciseId;
        this.order = order;
        this.series = new ArrayList<>();
    }

    // --- GETTERS E SETTERS ---
    public String getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(String workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }
    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getApiExerciseId() { return apiExerciseId != null ? apiExerciseId : ""; }
    public void setApiExerciseId(String apiExerciseId) { this.apiExerciseId = apiExerciseId; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }
    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    public void setSeries(List<Serie> series) { this.series = series; }
    public void addSerie(Serie serie) {
        getSeries().add(serie);
    }

    @Exclude
    public boolean isExpanded() { return isExpanded; }
    @Ignore
    @Exclude
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
    @Ignore
    @Exclude
    public String getMuscleGroup() { return muscleGroup; }
    @Ignore
    @Exclude
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

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