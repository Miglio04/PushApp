package com.example.pushapp.database;

import androidx.lifecycle.LiveData;
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

    @Query("DELETE FROM historySessions")
    void deleteAllHistory();

    @Transaction
    @Query("SELECT * FROM historySessions ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getAllHistory();

    @Transaction
    @Query("SELECT * FROM historySessions WHERE name LIKE '%' || :query || '%' " +
            "OR historySessionId IN (SELECT historySessionId FROM historyWorkoutExercises WHERE exerciseName LIKE '%' || :query || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> searchHistory(String query);

    @Query("DELETE FROM historySessions WHERE historySessionId = :historySessionId")
    void deleteSessionById(String historySessionId);

    @Query("SELECT s.startTime as date, MAX(ser.weight) as value " +
            "FROM historySessions s " +
            "JOIN historyWorkoutExercises ex ON s.historySessionId = ex.historySessionId " +
            "JOIN historySeries ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE LOWER(ex.exerciseName) = LOWER(:exerciseName) " +
            "GROUP BY s.historySessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getMaxWeightStats(String exerciseName);

    @Query("SELECT s.startTime as date, SUM(ser.weight * ser.reps) as value " +
            "FROM historySessions s " +
            "JOIN historyWorkoutExercises ex ON s.historySessionId = ex.historySessionId " +
            "JOIN historySeries ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE LOWER(ex.exerciseName) = LOWER(:exerciseName) " +
            "GROUP BY s.historySessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getTotalVolumeStats(String exerciseName);

    @Query("SELECT s.startTime as date, MAX(ser.weight * (1 + (ser.reps / 30.0))) as value " +
            "FROM historySessions s " +
            "JOIN historyWorkoutExercises ex ON s.historySessionId = ex.historySessionId " +
            "JOIN historySeries ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE LOWER(ex.exerciseName) = LOWER(:exerciseName) " +
            "GROUP BY s.historySessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getOneRepMaxStats(String exerciseName);

    @Transaction
    @Query("SELECT * FROM historySessions WHERE historySessionId IN " +
            "(SELECT historySessionId FROM historyWorkoutExercises WHERE exerciseName LIKE '%' || :exerciseName || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getHistoryByExercise(String exerciseName);
}