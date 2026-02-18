package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "serie",
        foreignKeys = @ForeignKey(
                entity = WorkoutExercise.class,
                parentColumns = "workoutExerciseId",
                childColumns = "workoutExerciseId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("workoutExerciseId"))
public class Serie implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "serieId")
    @NonNull
    private String serieId;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "workoutExerciseId")
    private String workoutExerciseId;
    @ColumnInfo(name = "targetReps")
    private int targetReps;
    @ColumnInfo(name = "targetWeight")
    private double targetWeight;

    public Serie() {
        this.serieId = UUID.randomUUID().toString();
        this.targetReps = 0;
        this.targetWeight = 0.0;
    }

    public Serie(int serieNumber, int targetReps, double targetWeight) {
        this.serieId = UUID.randomUUID().toString();
        this.targetReps = targetReps;
        this.targetWeight = targetWeight;
    }

    // Getters e Setters
    @Exclude
    public String getSerieId() { return serieId; }

    public void setSerieId(String serieId) { this.serieId = serieId; }
    @Exclude
    public String getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(String workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }
    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
