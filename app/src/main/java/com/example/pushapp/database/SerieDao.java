package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pushapp.models.Serie;

import java.util.List;

/**
 * Data Access Object (DAO) for accessing Serie data.
 * Manages operations for individual workout sets (series).
 */
@Dao
public interface SerieDao {
    /**
     * Inserts a single serie into the database.
     *
     * @param serie The serie to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Serie serie);

    /**
     * Inserts a list of series into the database.
     *
     * @param series The list of series to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Serie> series);

    /**
     * Updates an existing serie in the database.
     *
     * @param serie The serie with updated values.
     */
    @Update
    void update(Serie serie);

    /**
     * Deletes a serie from the database.
     *
     * @param serie The serie to delete.
     */
    @Delete
    void delete(Serie serie);

    /**
     * Retrieves a serie by its unique ID.
     *
     * @param id The ID of the serie.
     * @return The found Serie object or null if not found.
     */
    @Query("SELECT * FROM serie WHERE serieId = :id")
    Serie getById(int id);

    /**
     * Deletes all series associated with a specific workout exercise.
     *
     * @param workoutExerciseId The ID of the parent workout exercise.
     */
    @Query("DELETE FROM serie WHERE workoutExerciseId = :workoutExerciseId")
    void deleteByWorkoutExerciseId(int workoutExerciseId);

    /**
     * Deletes all series from the database.
     */
    @Query("DELETE FROM serie")
    void deteleAllSeries();
}
