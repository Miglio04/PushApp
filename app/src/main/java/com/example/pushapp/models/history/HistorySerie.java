package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Entity representing a recorded set in a workout history session.
 * Stores the actual performance data (reps, weight) achieved.
 */
@Entity(
        tableName = "historySeries",
        foreignKeys = @ForeignKey(
                entity = HistoryWorkoutExercise.class,
                parentColumns = "historyExerciseId",
                childColumns = "historyExerciseId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("historyExerciseId")}
)
public class HistorySerie {

    @PrimaryKey
    @ColumnInfo(name = "historySerieId")
    @NonNull
    private String historySerieId;
    @ColumnInfo(name = "userId")
    private String userId;
    @NonNull
    @ColumnInfo(name = "historyExerciseId")
    private String historyExerciseId;
    @ColumnInfo(name = "setNumber")
    private int setNumber;
    @ColumnInfo(name = "weight")
    private double weight;
    @ColumnInfo(name = "reps")
    private int reps;
    @Ignore
    private boolean isCompleted;

    /**
     * Default constructor.
     */
    public HistorySerie() {
        this.historySerieId = UUID.randomUUID().toString();
    }

    /**
     * Constructs a HistorySerie with performance data.
     *
     * @param historyExerciseId The ID of the parent history exercise.
     * @param setNumber         The ordinal number of the set.
     * @param weight            The weight used.
     * @param reps              The number of repetitions performed.
     */
    public HistorySerie(@NonNull String historyExerciseId, int setNumber, double weight, int reps) {
        this.historySerieId = UUID.randomUUID().toString();
        this.historyExerciseId = historyExerciseId;
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
    }

    @NonNull
    public String getHistorySerieId() { return historySerieId; }
    public void setHistorySerieId(@NonNull String historySerieId) { this.historySerieId = historySerieId; }

    @NonNull
    public String getHistoryExerciseId() { return historyExerciseId; }
    public void setHistoryExerciseId(@NonNull String historyExerciseId) { this.historyExerciseId = historyExerciseId; }

    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}