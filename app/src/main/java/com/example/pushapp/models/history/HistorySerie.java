package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "history_series",
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
    @NonNull
    public String historySetId;

    @NonNull
    public String historyExerciseId;

    public int setNumber;
    public double weight;
    public int reps;
    public boolean isPersonalRecord;

    public HistorySerie() {}

    public HistorySerie(@NonNull String historySetId, @NonNull String historyExerciseId, int setNumber, double weight, int reps) {
        this.historySetId = historySetId;
        this.historyExerciseId = historyExerciseId;
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
        this.isPersonalRecord = false;
    }

    @NonNull
    public String getHistorySetId() { return historySetId; }
    public void setHistorySetId(@NonNull String historySetId) { this.historySetId = historySetId; }

    @NonNull
    public String getHistoryExerciseId() { return historyExerciseId; }
    public void setHistoryExerciseId(@NonNull String historyExerciseId) { this.historyExerciseId = historyExerciseId; }

    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public boolean isPersonalRecord() { return isPersonalRecord; }
    public void setPersonalRecord(boolean personalRecord) { isPersonalRecord = personalRecord; }
}