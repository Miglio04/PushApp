package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pushapp.models.Serie;

import java.util.List;

@Dao
public interface SerieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Serie serie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Serie> series);

    @Update
    void update(Serie serie);

    @Delete
    void delete(Serie serie);

    @Query("SELECT * FROM serie WHERE serieId = :id")
    Serie getById(String id);

    @Query("SELECT * FROM serie WHERE workoutExerciseId = :workoutExerciseId ORDER BY serieNumber")
    List<Serie> getByWorkoutExerciseId(String workoutExerciseId);

    @Query("DELETE FROM serie WHERE workoutExerciseId = :workoutExerciseId")
    void deleteByWorkoutExerciseId(String workoutExerciseId);
}

