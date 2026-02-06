package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.pushapp.models.Training;
import com.example.pushapp.models.roomModels.helpers.TrainingWithRoutines;

import java.util.List;

@Dao
public interface TrainingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Training training);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Training> trainings);

    @Update
    void update(Training training);

    @Delete
    void delete(Training training);

    @Query("SELECT * FROM training WHERE trainingId = :id")
    Training getById(String id);

    @Query("SELECT * FROM training WHERE userId = :userId")
    List<Training> getByUserId(String userId);

    @Query("SELECT * FROM training WHERE userId = :userId AND isActive = 1")
    Training getActiveByUserId(String userId);

    @Transaction
    @Query("SELECT * FROM training WHERE trainingId = :id")
    TrainingWithRoutines getTrainingWithRoutines(String id);

    @Query("DELETE FROM training WHERE userId = :userId")
    void deleteAllByUserId(String userId);

    @Query("SELECT * FROM training")
    List<Training> getAllTrainings();

    @Transaction
    @Query("SELECT * FROM training")
    List<TrainingWithRoutines> getAllTrainingsWithRoutines();

    @Query("DELETE FROM training")
    void deleteAllTraings();

}

