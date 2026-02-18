package com.example.pushapp.repositories;

import com.example.pushapp.database.ExerciseDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseLocalDataSource  {

    private LocalDatabase localDatabase;
    private ExerciseDao exerciseDao;
    private ExerciseCallback callback = null;


    ExerciseLocalDataSource(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.exerciseDao = localDatabase.exerciseDao();
    }

    public void setCallback(ExerciseRepository exerciseRepository) {
        this.callback = exerciseRepository;
    }

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

    public void deleteExercises(){
        LocalDatabase.databaseWriteExecutor.execute(()->{
            localDatabase.runInTransaction(() -> {
                try {
                    exerciseDao.deleteAll();
                    callback.onSuccessFromLocalDelete();
                } catch (Exception e) {
                    callback.onFailureFromLocal(e);
                }
            });
        });
    }


}
