package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "history_workout_exercises",
        foreignKeys = @ForeignKey(
                entity = HistorySession.class,
                parentColumns = "sessionId",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("sessionId")}
)
public class HistoryWorkoutExercise {

    @PrimaryKey
    @NonNull
    public String historyExerciseId;

    @NonNull
    public String sessionId;         // Chiave esterna verso HistorySession

    public String exerciseName;
    public int orderIndex;

    public HistoryWorkoutExercise() {}

    public HistoryWorkoutExercise(@NonNull String historyExerciseId, @NonNull String sessionId, String exerciseName, int orderIndex) {
        this.historyExerciseId = historyExerciseId;
        this.sessionId = sessionId;
        this.exerciseName = exerciseName;
        this.orderIndex = orderIndex;
    }

    // --- Getters e Setters ---
    @NonNull
    public String getHistoryExerciseId() { return historyExerciseId; }
    public void setHistoryExerciseId(@NonNull String historyExerciseId) { this.historyExerciseId = historyExerciseId; }

    @NonNull
    public String getSessionId() { return sessionId; }
    public void setSessionId(@NonNull String sessionId) { this.sessionId = sessionId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}