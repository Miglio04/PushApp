package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.ExerciseWithSeries;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Exercise exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Exercise> exercises);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercise WHERE baseExerciseId = :id")
    Exercise getById(String id);

    @Query("SELECT * FROM exercise WHERE trainingDayId = :trainingDayId ORDER BY exerciseOrder")
    List<Exercise> getByTrainingDayId(String trainingDayId);

    @Transaction
    @Query("SELECT * FROM exercise WHERE baseExerciseId = :id")
    ExerciseWithSeries getWithSeries(String id);

    @Query("DELETE FROM exercise WHERE trainingDayId = :trainingDayId")
    void deleteByTrainingDayId(String trainingDayId);
}
