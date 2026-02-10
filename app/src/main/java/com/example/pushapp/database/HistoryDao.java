package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(HistorySession session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(HistoryWorkoutExercise exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSerie(HistorySerie serie);

    @Transaction
    @Query("SELECT * FROM history_sessions ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getAllHistory();

    @Transaction
    @Query("SELECT * FROM history_sessions WHERE name LIKE '%' || :query || '%' " +
            "OR sessionId IN (SELECT sessionId FROM history_workout_exercises WHERE exerciseName LIKE '%' || :query || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> searchHistory(String query);

    @Query("DELETE FROM history_sessions WHERE sessionId = :sessionId")
    void deleteSessionById(String sessionId);

    @Query("SELECT s.startTime as date, MAX(ser.weight) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getMaxWeightStats(String exerciseName);

    @Query("SELECT s.startTime as date, SUM(ser.weight * ser.reps) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getTotalVolumeStats(String exerciseName);

    @Query("SELECT s.startTime as date, MAX(ser.weight * (1 + (ser.reps / 30.0))) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getOneRepMaxStats(String exerciseName);

    @Transaction
    @Query("SELECT * FROM history_sessions WHERE sessionId IN " +
            "(SELECT sessionId FROM history_workout_exercises WHERE exerciseName LIKE '%' || :exerciseName || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getHistoryByExercise(String exerciseName);
}