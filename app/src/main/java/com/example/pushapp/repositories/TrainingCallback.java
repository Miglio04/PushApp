package com.example.pushapp.repositories;

import com.example.pushapp.models.Training;

import java.util.List;

public interface TrainingCallback {
    void onSuccessFromLocal(List<Training> trainingsList);
    void onFailureFromLocal(Exception exception);
    void onFailureFromRemote(Exception exception);
}
