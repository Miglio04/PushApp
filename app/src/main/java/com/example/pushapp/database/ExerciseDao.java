package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<Exercise> exercises);

    @Query("DELETE FROM exercise")
    void deleteAll();

    @Query("SELECT * FROM exercise")
    List<Exercise> getAllExercises();
}
