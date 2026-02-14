package com.example.pushapp.repositories;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;

import java.util.List;

public interface TrainingCallback {

    void onSuccessFromLocalTrainingFetch(String userId, List<Training> finalTrainingList);
    void onSuccessFromLocalTrainingGet(List<Training> finalTrainingList);
    void onSuccessFromLocalTrainingCreate(String userId, Training training);
    void onSuccessFromLocalTrainingDelete(Training training);
    void onSuccessFromLocalTrainingUpdate(Training training);
    void onSuccessFromLocalRoutineCreate(Routine routine);
    void onSuccessFromLocalRoutineUpdate(Routine routine);
    void onSuccessFromLocalRoutineDelete(Routine routine);
    void onSuccessFromRemote(List<Training> trainingsList);
    void onFailureFromLocal(Exception exception);
    void onFailureFromRemote(Exception exception);

}
