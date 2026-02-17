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

/**
 * Data Access Object (DAO) for managing workout history data.
 * Handles operations for HistorySession, HistoryWorkoutExercise, and HistorySerie tables.
 * Includes complex queries for statistics and searching.
 */
@Dao
public interface HistoryDao {

    /**
     * Inserts a completed specific workout session into the database.
     *
     * @param session The history session to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(HistorySession session);

    /**
     * Inserts a workout exercise record associated with a history session.
     *
     * @param exercise The history workout exercise to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(HistoryWorkoutExercise exercise);

    /**
     * Inserts a series record associated with a history workout exercise.
     *
     * @param serie The history series to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSerie(HistorySerie serie);

    /**
     * Deletes all history data from the database.
     */
    @Query("DELETE FROM historySessions")
    void deleteAllHistory();

    /**
     * Retrieves all history sessions, including their nested exercises and series.
     * Ordered by start time descending.
     *
     * @return A list of HistorySessionWithExercises objects.
     */
    @Transaction
    @Query("SELECT * FROM historySessions ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getAllHistory();

    /**
     * Searches for history sessions where the session name or any exercise name matches the query.
     *
     * @param query The search text to match.
     * @return A filtered list of HistorySessionWithExercises.
     */
    @Transaction
    @Query("SELECT * FROM historySessions WHERE name LIKE '%' || :query || '%' " +
            "OR historySessionId IN (SELECT historySessionId FROM historyWorkoutExercises WHERE exerciseName LIKE '%' || :query || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> searchHistory(String query);

    /**
     * Deletes a specific history session by its ID.
     * Cascading deletion handles associated exercises and series.
     *
     * @param historySessionId The ID of the session to delete.
     */
    @Query("DELETE FROM historySessions WHERE historySessionId = :historySessionId")
    void deleteSessionById(String historySessionId);

    /**
     * Calculates the maximum weight lifted for a specific exercise over time.
     * Used for generating progress charts.
     *
     * @param exerciseName The name of the exercise to analyze.
     * @return A list of GraphPoints containing date and max weight values.
     */
    @Query("SELECT s.startTime as date, MAX(ser.weight) as value " +
            "FROM historySessions s " +
            "JOIN historyWorkoutExercises ex ON s.historySessionId = ex.historySessionId " +
            "JOIN historySeries ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE LOWER(ex.exerciseName) = LOWER(:exerciseName) " +
            "GROUP BY s.historySessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getMaxWeightStats(String exerciseName);

    /**
     * Calculates the total volume (reps * weight) lifted for a specific exercise per session over time.
     *
     * @param exerciseName The name of the exercise to analyze.
     * @return A list of GraphPoints containing date and total volume values.
     */
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