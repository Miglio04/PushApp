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

/**
 * Data Access Object (DAO) for accessing Training data.
 * Manages operations related to training plans and their hierarchical retrieval.
 */
@Dao
public interface TrainingDao {
    /**
     * Inserts a single training into the database.
     *
     * @param training The training to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Training training);

    /**
     * Inserts a list of trainings into the database.
     *
     * @param trainings The list of trainings to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Training> trainings);

    /**
     * Updates an existing training record.
     *
     * @param training The training with updated data.
     */
    @Update
    void update(Training training);

    /**
     * Deletes a training record from the database.
     *
     * @param training The training to delete.
     */
    @Delete
    void delete(Training training);

    /**
     * Retrieves a training by its unique ID.
     *
     * @param id The ID of the training.
     * @return The Training object.
     */
    @Query("SELECT * FROM training WHERE trainingId = :id")
    Training getById(String id);

    /**
     * Retrieves all trainings belonging to a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of Training objects.
     */
    @Query("SELECT * FROM training WHERE userId = :userId")
    List<Training> getByUserId(String userId);

    /**
     * Retrieves the currently active training for a user (placeholder logic).
     *
     * @param userId The ID of the user.
     * @return The active Training object.
     */
    @Query("SELECT * FROM training WHERE userId = :userId")
    Training getActiveByUserId(String userId);

    /**
     * Retrieves a training with all its associated routines by training ID.
     *
     * @param id The ID of the training.
     * @return A TrainingWithRoutines object.
     */
    @Transaction
    @Query("SELECT * FROM training WHERE trainingId = :id")
    TrainingWithRoutines getTrainingWithRoutines(String id);

    /**
     * Deletes all trainings associated with a specific user.
     *
     * @param userId The ID of the user.
     */
    @Query("DELETE FROM training WHERE userId = :userId")
    void deleteAllByUserId(String userId);

    /**
     * Retrieves all trainings in the database.
     *
     * @return A list of all Training objects.
     */
    @Query("SELECT * FROM training")
    List<Training> getAllTrainings();

    /**
     * Retrieves all trainings with their associated routines.
     *
     * @return A list of TrainingWithRoutines objects.
     */
    @Transaction
    @Query("SELECT * FROM training")
    List<TrainingWithRoutines> getAllTrainingsWithRoutines();

    /**
     * Deletes all trainings from the database.
     */
    @Query("DELETE FROM training")
    void deleteAllTraings();
}
