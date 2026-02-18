package com.example.pushapp.repositories;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public interface ExerciseCallback {
    void onSuccessFromRemote(ArrayList<Exercise> exerciseList);
    void onFailureFromRemote(Exception e);

    void onSuccessFromLocalDelete();
    void onSuccessFromLocalGet(List<Exercise> exercises);
    void onFailureFromLocal(Exception e);
}
