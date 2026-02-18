package com.example.pushapp.repositories;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import java.util.List;

public interface HistoryCallback {
    default void onSuccessSaveLocal() {}

    default void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list) {}

    default void onSuccessGraphDataFromLocal(List<GraphPoint> points) {}

    default void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {}

    default void onFailureFromLocal(Exception e) {}

    default void onFailureFromRemote(Exception e) {}
}