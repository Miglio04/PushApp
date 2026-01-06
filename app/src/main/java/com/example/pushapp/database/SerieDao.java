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

    @Query("SELECT * FROM serie WHERE id = :id")
    Serie getById(String id);

    @Query("SELECT * FROM serie WHERE exerciseId = :exerciseId ORDER BY serieNumber")
    List<Serie> getByExerciseId(String exerciseId);

    @Query("DELETE FROM serie WHERE exerciseId = :exerciseId")
    void deleteByExerciseId(String exerciseId);
}

