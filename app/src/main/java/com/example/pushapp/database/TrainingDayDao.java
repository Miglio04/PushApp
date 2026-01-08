package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.TrainingDayWithExercises;

import java.util.List;

@Dao
public interface TrainingDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TrainingDay trainingDay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TrainingDay> trainingDays);

    @Update
    void update(TrainingDay trainingDay);

    @Delete
    void delete(TrainingDay trainingDay);

    @Query("SELECT * FROM trainingDay WHERE trainingDayId = :id")
    TrainingDay getById(String id);

    @Query("SELECT * FROM trainingDay WHERE trainingDayId = :trainingId ORDER BY dayOrder")
    List<TrainingDay> getByTrainingId(String trainingId);

    @Transaction
    @Query("SELECT * FROM trainingDay WHERE trainingDayId = :id")
    TrainingDayWithExercises getWithExercises(String id);

    @Query("DELETE FROM trainingDay WHERE trainingId = :trainingId")
    void deleteByTrainingId(String trainingId);
}
