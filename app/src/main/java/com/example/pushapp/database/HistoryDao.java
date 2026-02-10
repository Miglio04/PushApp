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

    // --- SALVATAGGIO (Scrittura) ---
    // 1. Inserisci la sessione
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(HistorySession session);

    // 2. Inserisci un esercizio (Aggiornato: insertWorkoutExercise)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(HistoryWorkoutExercise exercise);

    // 3. Inserisci una serie
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSerie(HistorySerie serie);

    // --- LETTURA (Query) ---
    // 4. Ottieni tutto lo storico
    @Transaction
    @Query("SELECT * FROM history_sessions ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getAllHistory();

    // 5. Cerca nello storico per nome esercizio
    @Transaction
    @Query("SELECT * FROM history_sessions WHERE sessionId IN " +
            "(SELECT sessionId FROM history_workout_exercises WHERE exerciseName LIKE '%' || :exerciseName || '%') " +
            "ORDER BY startTime DESC")
    List<HistorySessionWithExercises> getHistoryByExercise(String exerciseName);

    // --- ELIMINAZIONE ---
    // 6. Elimina una sessione specifica
    @Query("DELETE FROM history_sessions WHERE sessionId = :sessionId")
    void deleteSessionById(String sessionId);

    // --- GRAFICI (Aggregazione Dati) ---
    // 7. Grafico Massimali Reali (MAX Weight sollevato)
    @Query("SELECT s.startTime as date, MAX(ser.weight) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getMaxWeightStats(String exerciseName);

    // 8. Grafico Volume Totale (Somma Peso * Reps)
    @Query("SELECT s.startTime as date, SUM(ser.weight * ser.reps) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getTotalVolumeStats(String exerciseName);

    // 9. Grafico Massimale Stimato (1RM - Epley Formula)
    @Query("SELECT s.startTime as date, MAX(ser.weight * (1 + (ser.reps / 30.0))) as value " +
            "FROM history_sessions s " +
            "JOIN history_workout_exercises ex ON s.sessionId = ex.sessionId " +
            "JOIN history_series ser ON ex.historyExerciseId = ser.historyExerciseId " +
            "WHERE ex.exerciseName = :exerciseName " +
            "GROUP BY s.sessionId " +
            "ORDER BY s.startTime ASC")
    List<GraphPoint> getOneRepMaxStats(String exerciseName);
}