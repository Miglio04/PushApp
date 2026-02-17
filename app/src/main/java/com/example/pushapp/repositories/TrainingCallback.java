package com.example.pushapp.repositories;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;

import java.util.List;

/**
 * Callback interface for handling Training repository operations.
 * Provides specific methods for success and failure regarding Trainings and Routines,
 * distinguishing between local database and remote updates.
 */
public interface TrainingCallback {

    void onSuccessFromLocalTrainingFetch(String userId, List<Training> finalTrainingList);
    void onSuccessFromLocalTrainingGet(List<Training> finalTrainingList);
    void onSuccessFromLocalTrainingCreate(String userId, Training training);
    void onSuccessFromLocalTrainingDelete(Training training);
    void onSuccessFromLocalTrainingUpdate(Training training);
    void onSuccessFromLocalRoutineCreate(Routine routine);
    void onSuccessFromLocalRoutineUpdate(Routine routine);
    void onSuccessFromLocalRoutineDelete(Routine routine);
    void onSuccessFromRemote(List<Training> trainingsList, String userId);
    void onFailureFromLocal(Exception exception);
    void onFailureFromRemote(Exception exception);

}
