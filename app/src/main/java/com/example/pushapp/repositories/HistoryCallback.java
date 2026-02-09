package com.example.pushapp.repositories;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import java.util.List;

public interface HistoryCallback {
    void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list);
    void onSuccessGraphDataFromLocal(List<GraphPoint> points);
    void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteData);
    void onSuccessSaveLocal();
    void onFailureFromLocal(Exception e);
    void onFailureFromRemote(Exception e);
}