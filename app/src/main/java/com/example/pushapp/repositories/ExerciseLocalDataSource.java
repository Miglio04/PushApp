package com.example.pushapp.repositories;

import com.example.pushapp.database.ExerciseDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Data source for handling exercise-related operations with the local Room database.
 * Executes database operations asynchronously and communicates results via callbacks.
 * Manages caching of Exercise objects locally.
 */
public class ExerciseLocalDataSource  {

    private final LocalDatabase localDatabase;
    private final ExerciseDao exerciseDao;
    private ExerciseCallback callback = null;

    ExerciseLocalDataSource(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.exerciseDao = localDatabase.exerciseDao();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param exerciseRepository The callback implementation (typically the Repository).
     */
    public void setCallback(ExerciseRepository exerciseRepository) {
        this.callback = exerciseRepository;
    }

    /**
     * Retrieves all exercises from the local database.
     * Executed on a background thread.
     */
    public void getExercises(){
        LocalDatabase.databaseWriteExecutor.execute(()->{
            try{
                List<Exercise> exercises = exerciseDao.getAllExercises();
                callback.onSuccessFromLocalGet(exercises);
            } catch (Exception e) {
                callback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Replaces all exercises in the local database transactionally.
     * Deletes existing data and inserts the new list, then triggers a refresh.
     *
     * @param exercises The list of exercises to insert.
     */
    public void insertExercises(ArrayList<Exercise> exercises){
        LocalDatabase.databaseWriteExecutor.execute(()->{
            localDatabase.runInTransaction(() -> {
                try{
                    exerciseDao.deleteAll();
                    exerciseDao.insertAll(exercises);
                } catch (Exception e) {
                    callback.onFailureFromLocal(e);
                }
            });
            getExercises();
        });
    }

    /**
     * Clears all exercises from the local database.
     */
    public void deleteExercises(){
        LocalDatabase.databaseWriteExecutor.execute(()->
            localDatabase.runInTransaction(() -> {
                try {
                    exerciseDao.deleteAll();
                    callback.onSuccessFromLocalDelete();
                } catch (Exception e) {
                    callback.onFailureFromLocal(e);
                }
        }));
    }


}
