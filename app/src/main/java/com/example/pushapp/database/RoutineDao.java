package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.roomModels.helpers.RoutineWithWorkoutExercises;

import java.util.List;

/**
 * Data Access Object (DAO) for accessing Routine data.
 * Manages operations related to workout routines and their relationships with exercises.
 */
@Dao
public interface RoutineDao {
    /**
     * Inserts a single routine into the database.
     * Replaces on conflict.
     *
     * @param routine The routine to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Routine routine);

    /**
     * Inserts a list of routines into the database.
     * Replaces on conflict.
     *
     * @param routines The list of routines to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Routine> routines);

    /**
     * Updates an existing routine in the database.
     *
     * @param routine The routine with updated values.
     */
    @Update
    void update(Routine routine);

    /**
     * Deletes a routine from the database.
     *
     * @param routine The routine to delete.
     */
    @Delete
    void delete(Routine routine);

    /**
     * Retrieves a routine by its ID, including all associated workout exercises.
     *
     * @param id The ID of the routine.
     * @return A RoutineWithWorkoutExercises object populated with data.
     */
    @Transaction
    @Query("SELECT * FROM Routine WHERE routineId = :id")
    RoutineWithWorkoutExercises getWithExercises(String id);

    /**
     * Deletes all routines associated with a specific training plan.
     *
     * @param trainingId The ID of the parent training plan.
     */
    @Query("DELETE FROM Routine WHERE trainingId = :trainingId")
    void deleteByTrainingId(String trainingId);

    /**
     * Retrieves all routines for a given training plan, including their exercises.
     *
     * @param trainingId The ID of the training plan.
     * @return A list of RoutineWithWorkoutExercises.
     */
    @Transaction
    @Query("SELECT * FROM Routine WHERE trainingId = :trainingId")
    List<RoutineWithWorkoutExercises> getRoutinesWithExercisesByTrainingId(String trainingId);

    /**
     * Deletes all routines from the database.
     */
    @Query("DELETE FROM routine")
    void deteleAllRoutines();
}
