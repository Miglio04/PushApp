package com.example.pushapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "serie",
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = "id",
                childColumns = "exerciseId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("exerciseId")
)
public class Serie implements Serializable {
    @PrimaryKey
    private int id;
    private int exerciseId;
    @ColumnInfo(name = "serieNumber")
    private int serieNumber;
    @ColumnInfo(name = "targetReps")
    private int targetReps;
    @ColumnInfo(name = "targetWeight")
    private double targetWeight;
    @ColumnInfo(name = "actualReps")
    private int actualReps;
    @ColumnInfo(name = "actualWeight")
    private double actualWeight;
    @ColumnInfo(name = "completed")
    private boolean completed;

    public Serie() {}

    public Serie(int serieNumber, int targetReps, double targetWeight) {
        this.serieNumber = serieNumber;
        this.targetReps = targetReps;
        this.targetWeight = targetWeight;
        this.completed = false;
    }

    // Getters e Setters
    public int getSerieNumber() { return serieNumber; }
    public void setSerieNumber(int serieNumber) { this.serieNumber = serieNumber; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public int getActualReps() { return actualReps; }
    public void setActualReps(int actualReps) { this.actualReps = actualReps; }

    public double getActualWeight() { return actualWeight; }
    public void setActualWeight(double actualWeight) { this.actualWeight = actualWeight; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
}
