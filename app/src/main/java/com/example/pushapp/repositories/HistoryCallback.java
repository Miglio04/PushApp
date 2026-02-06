package com.example.pushapp.repositories;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import java.util.List;

public interface HistoryCallback {
    // Risposte dal Locale
    void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> historyList);
    void onSuccessGraphDataFromLocal(List<GraphPoint> points);
    void onSuccessSaveLocal();
    void onFailureFromLocal(Exception e);

    // Risposte dal Remoto
    void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteHistory);
    void onFailureFromRemote(Exception e);
}