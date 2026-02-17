package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.WorkoutExerciseWithSeries;

import java.util.List;

/**
 * Data Access Object (DAO) for accessing WorkoutExercise data.
 * Manages operations for exercises within a routine and their sets.
 */
@Dao
public interface WorkoutExerciseDao {
    /**
     * Inserts a single workout exercise into the database.
     *
     * @param workoutExercise The exercise to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutExercise workoutExercise);

    /**
     * Inserts a list of workout exercises.
     *
     * @param workoutExercises The list of exercises to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkoutExercise> workoutExercises);

    /**
     * Updates an existing workout exercise.
     *
     * @param workoutExercise The exercise to update.
     */
    @Update
    void update(WorkoutExercise workoutExercise);

    /**
     * Deletes a workout exercise from the database.
     *
     * @param workoutExercise The exercise to delete.
     */
    @Delete
    void delete(WorkoutExercise workoutExercise);

    /**
     * Retrieves a workout exercise by its unique integer ID.
     *
     * @param id The ID of the workout exercise.
     * @return The WorkoutExercise object.
     */
    @Query("SELECT * FROM WorkoutExercise WHERE workoutExerciseId = :id")
    WorkoutExercise getById(int id);

    /**
     * Retrieves all exercises associated with a specific routine.
     *
     * @param routineId The ID of the routine.
     * @return A list of WorkoutExercise objects.
     */
    @Query("SELECT * FROM WorkoutExercise WHERE routineId = :routineId")
    List<WorkoutExercise> getByRoutineId(String routineId);

    /**
     * Retrieves a workout exercise along with its associated series.
     *
     * @param id The ID of the workout exercise.
     * @return A WorkoutExerciseWithSeries helper object.
     */
    @Transaction
    @Query("SELECT * FROM WorkoutExercise WHERE workoutExerciseId = :id")
    WorkoutExerciseWithSeries getWorkoutExerciseWithSeries(int id);

    /**
     * Deletes all workout exercises associated with a specific routine.
     *
     * @param routineId The ID of the routine.
     */
    @Query("DELETE FROM WorkoutExercise WHERE routineId = :routineId")
    void deleteByRoutineId(String routineId);

    /**
     * Retrieves list of exercises complete with their series for a specific routine.
     *
     * @param routineId The ID of the routine.
     * @return A list of WorkoutExerciseWithSeries objects.
     */
    @Transaction
    @Query("SELECT * FROM WorkoutExercise WHERE routineId = :routineId")
    List<WorkoutExerciseWithSeries> getExercisesWithSeriesByRoutineId(String routineId);

    /**
     * Deletes all workout exercises from the database.
     */
    @Query("DELETE FROM workoutExercise")
    void deteleAllWorkoutExercises();

}
