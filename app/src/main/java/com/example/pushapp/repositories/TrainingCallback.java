package com.example.pushapp.repositories;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;

import java.util.List;

public interface TrainingCallback {

    void onSuccessFromLocalFetch(String userId, List<Training> finalTrainingList);
    void onSuccessFromLocalGet(List<Training> finalTrainingList);
    void onSuccessFromLocalCreate(String userId, Training training);
    void onSuccessFromLocalDelete(Training training);
    void onSuccessFromLocalRoutineUpdate(Routine routine);
    void onSuccessFromRemote(List<Training> trainingsList);
    void onFailureFromLocal(Exception exception);
    void onFailureFromRemote(Exception exception);
}
