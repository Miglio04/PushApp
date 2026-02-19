package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for accessing Exercise data in the local database.
 * Provides methods for inserting and retrieving standard exercise definitions.
 */
@Dao
public interface ExerciseDao {

    /**
     * Inserts a list of exercises into the database.
     * Replaces existing entries on conflict.
     *
     * @param exercises The list of exercises to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<Exercise> exercises);

    /**
     * Deletes all exercises from the database table.
     */
    @Query("DELETE FROM exercise")
    void deleteAll();

    /**
     * Retrieves all exercises stored in the database.
     *
     * @return A list of all exercises.
     */
    @Query("SELECT * FROM exercise")
    List<Exercise> getAllExercises();
}
