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

@Dao
public interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Routine routine);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Routine> routines);

    @Update
    void update(Routine routine);

    @Delete
    void delete(Routine routine);

    @Query("SELECT * FROM Routine WHERE routineId = :id")
    Routine getById(String id);

    @Query("SELECT * FROM Routine WHERE routineId = :trainingId ORDER BY dayOrder")
    List<Routine> getByTrainingId(String trainingId);

    @Transaction
    @Query("SELECT * FROM Routine WHERE routineId = :id")
    RoutineWithWorkoutExercises getWithExercises(String id);

    @Query("DELETE FROM Routine WHERE trainingId = :trainingId")
    void deleteByTrainingId(String trainingId);
}
