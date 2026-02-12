package com.example.pushapp.repositories;

import com.example.pushapp.models.Training;

import java.util.List;

public interface TrainingCallback {
    void onSuccessFromLocal(List<Training> trainingsList);
    void onFailureFromLocal(Exception exception);
    void onSuccessFromRemote(List<Training> trainingsList);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocalFetch(String userId, List<Training> finalTrainingList);
    void onSuccessFromLocalGet(List<Training> finalTrainingList);
    void onSuccessFromLocalCreate(String userId, Training training);
    void onSuccessFromLocalDelete(Training training);
}
