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

/**
 * Entity representing an exercise performed during a history session.
 * links the session to the individual series performed.
 */
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
    private String historyExerciseId;
    @ColumnInfo(name = "userId")
    private String userId;
    @NonNull
    @ColumnInfo(name = "historySessionId")
    private String historySessionId;
    @ColumnInfo(name = "exerciseName")
    private String exerciseName;
    @ColumnInfo(name = "orderIndex")
    private int orderIndex;
    @Ignore
    private int currentRestTimeIndex = -1;

    /**
     * Default constructor.
     */
    public HistoryWorkoutExercise() {
        this.historyExerciseId = UUID.randomUUID().toString();
    }

    /**
     * Constructs a HistoryWorkoutExercise.
     *
     * @param historySessionId The ID of the parent session.
     * @param exerciseName     The name of the exercise.
     * @param orderIndex       The order of the exercise in the session.
     */
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
    public int getCurrentRestTimeIndex() { return currentRestTimeIndex; }
    public void setCurrentRestTimeIndex(int currentRestTimeIndex) { this.currentRestTimeIndex = currentRestTimeIndex; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}