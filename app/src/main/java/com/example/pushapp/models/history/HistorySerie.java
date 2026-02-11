package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

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
    public String historySerieId;

    @NonNull
    @ColumnInfo(name = "historyExerciseId")
    public String historyExerciseId;
    @ColumnInfo(name = "setNumber")
    public int setNumber;
    @ColumnInfo(name = "weight")
    public double weight;
    @ColumnInfo(name = "reps")
    public int reps;
    @ColumnInfo(name = "isCompleted")
    public boolean isCompleted;
    @ColumnInfo(name = "isPersonalRecord")
    public boolean isPersonalRecord;

    public HistorySerie() {
        this.historySerieId = UUID.randomUUID().toString();
    }

    public HistorySerie(@NonNull String historyExerciseId, int setNumber, double weight, int reps) {
        this.historySerieId = UUID.randomUUID().toString();
        this.historyExerciseId = historyExerciseId;
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
        this.isPersonalRecord = false;
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
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    public boolean isPersonalRecord() { return isPersonalRecord; }
    public void setPersonalRecord(boolean personalRecord) { isPersonalRecord = personalRecord; }
}