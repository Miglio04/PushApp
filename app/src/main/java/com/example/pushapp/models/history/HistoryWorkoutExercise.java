package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.List;
import java.util.UUID;

@Entity(
        tableName = "historyWorkoutExercises",
        foreignKeys = @ForeignKey(
                entity = HistorySession.class,
                parentColumns = "historySessionId",
                childColumns = "historySessionId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("historySessionId")}
)
public class HistoryWorkoutExercise {

    @PrimaryKey
    @ColumnInfo(name = "historyExerciseId")
    @NonNull
    public String historyExerciseId;

    @NonNull
    @ColumnInfo(name = "historySessionId")
    public String historySessionId;

    @ColumnInfo(name = "exerciseName")

    public String exerciseName;
    @ColumnInfo(name = "orderIndex")

    public int orderIndex;
    @Ignore
    public int currentRestTimeIndex = -1;
    @Ignore
    public List<HistorySerie> historySerieList;

    public HistoryWorkoutExercise() {
        this.historyExerciseId = UUID.randomUUID().toString();
    }

    public HistoryWorkoutExercise(@NonNull String historySessionId, String exerciseName, int orderIndex) {
        this.historyExerciseId = UUID.randomUUID().toString();
        this.historySessionId = historySessionId;
        this.exerciseName = exerciseName;
        this.orderIndex = orderIndex;
    }

    @NonNull
    public String getHistoryExerciseId() { return historyExerciseId; }
    public void setHistoryExerciseId(@NonNull String historyExerciseId) { this.historyExerciseId = historyExerciseId; }

    @NonNull
    public String getHistorySessionId() { return historySessionId; }
    public void setHistorySessionId(@NonNull String historySessionId) { this.historySessionId = historySessionId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}