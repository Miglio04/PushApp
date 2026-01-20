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

@Dao
public interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutExercise workoutExercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkoutExercise> workoutExercises);

    @Update
    void update(WorkoutExercise workoutExercise);

    @Delete
    void delete(WorkoutExercise workoutExercise);

    @Query("SELECT * FROM WorkoutExercise WHERE workoutExerciseId = :id")
    WorkoutExercise getById(String id);

    @Query("SELECT * FROM WorkoutExercise WHERE routineId = :routineId ORDER BY exerciseOrder")
    List<WorkoutExercise> getByRoutineId(String routineId);

    @Transaction
    @Query("SELECT * FROM WorkoutExercise WHERE workoutExerciseId = :id")
    WorkoutExerciseWithSeries getWorkoutExerciseWithSeries(String id);

    @Query("DELETE FROM WorkoutExercise WHERE routineId = :routineId")
    void deleteByRoutineId(String routineId);
}
