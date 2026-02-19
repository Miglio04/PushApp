package com.example.pushapp.repositories;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Callback interface for handling Exercise repository operations.
 * Provides methods for receiving success and failure events from both
 * local database and remote API data sources regarding Exercise data.
 */
public interface ExerciseCallback {
    void onSuccessFromRemote(ArrayList<Exercise> exerciseList);
    void onFailureFromRemote(Exception e);

    void onSuccessFromLocalDelete();
    void onSuccessFromLocalGet(List<Exercise> exercises);
    void onFailureFromLocal(Exception e);
}
