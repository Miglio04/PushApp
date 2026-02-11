package com.example.pushapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository implements HistoryCallback {
    private final String TAG = "HistoryRepository";
    private final HistoryLocalDataSource localDataSource;
    private final HistoryRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> historyList = new MutableLiveData<>();
    private final MutableLiveData<Result> graphData = new MutableLiveData<>();

    public enum StatMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }

    public HistoryRepository(HistoryLocalDataSource localDataSource, HistoryRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setHistoryCallback(this);
        if (this.remoteDataSource != null) {
            this.remoteDataSource.setCallback(this);
        }
    }

    public LiveData<Result> getHistoryList() {
        localDataSource.getAllHistory();
        if (remoteDataSource != null) {
            remoteDataSource.fetchHistoryFromRemote();
        }
        return historyList;
    }

    public void searchHistory(String query) {
        if (query == null || query.isEmpty()) {
            localDataSource.getAllHistory();
        } else {
            localDataSource.searchHistory(query);
        }
    }

    public HistorySessionWithExercises createNewWorkoutSession(Routine day) {
        if (day == null) return null;
        long workoutStartTimeMillis = System.currentTimeMillis();

        HistorySession newSession = new HistorySession(day.getName(), workoutStartTimeMillis, 0);

        List<HistoryWorkoutExerciseWithSeries> historyExercises = new ArrayList<>();
        for (WorkoutExercise woEx : day.getWorkoutExercises()) {

            HistoryWorkoutExercise hExercise = new HistoryWorkoutExercise(
                    newSession.getHistorySessionId(),
                    woEx.getApiExerciseId(),
                    day.getWorkoutExercises().indexOf(woEx)
            );

            List<HistorySerie> historySeries = new ArrayList<>();
            for (Serie templateSerie : woEx.getSeries()) {
                HistorySerie hSerie = new HistorySerie(
                        hExercise.getHistoryExerciseId(),
                        templateSerie.getSerieNumber(),
                        0,
                        0
                );
                historySeries.add(hSerie);
            }

            hExercise.currentRestTimeIndex = woEx.getRestTimeIndex();
            HistoryWorkoutExerciseWithSeries exerciseWithSeries = new HistoryWorkoutExerciseWithSeries();
            exerciseWithSeries.historyWorkoutExercise = hExercise;
            exerciseWithSeries.historySeries = historySeries;
            historyExercises.add(exerciseWithSeries);
        }

        HistorySessionWithExercises sessionWithExercises = new HistorySessionWithExercises();
        sessionWithExercises.session = newSession;
        sessionWithExercises.exercises = historyExercises;

        return sessionWithExercises;
    }

    public void saveWorkoutSession(HistorySessionWithExercises sessionToSave, Runnable onComplete) {
        if (sessionToSave == null) {
            // Se non c'è niente da salvare, esci
            if (onComplete != null) onComplete.run();
            return;
        }

        HistorySession session = sessionToSave.session;
        List<HistoryWorkoutExercise> historyExercises = new ArrayList<>();
        List<HistorySerie> historySeries = new ArrayList<>();

        if (sessionToSave.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries exerciseWithSeries : sessionToSave.exercises) {
                historyExercises.add(exerciseWithSeries.historyWorkoutExercise);
                if (exerciseWithSeries.historySeries != null) {
                    historySeries.addAll(exerciseWithSeries.historySeries);
                }
            }
        }

        localDataSource.saveSession(session, historyExercises, historySeries, new HistoryCallback() {
            @Override
            public void onSuccessSaveLocal() {
                if (remoteDataSource != null) {
                    //remoteDataSource.uploadWorkoutSession(sessionToSave);
                }
                localDataSource.getAllHistory();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
            @Override
            public void onFailureFromLocal(Exception e) {
                historyList.postValue(new Result.Error(e.getMessage()));
            }
            @Override public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> l) {}
            @Override public void onSuccessGraphDataFromLocal(List<GraphPoint> p) {}
            @Override public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> l) {}
            @Override public void onFailureFromRemote(Exception e) {}
        });
    }

    public void deleteSession(String sessionId) {
        localDataSource.deleteSession(sessionId);
        if (remoteDataSource != null) {
            remoteDataSource.deleteSession(sessionId);
        }
        localDataSource.getAllHistory();
    }

    public LiveData<Result> getGraphData(String exerciseName, StatMetric metric) {
        localDataSource.getGraphData(exerciseName, metric);
        return graphData;
    }

    @Override
    public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list) {
        historyList.postValue(new Result.HistorySuccess(list));
    }

    @Override
    public void onSuccessGraphDataFromLocal(List<GraphPoint> points) {
        graphData.postValue(new Result.GraphSuccess(points));
    }

    @Override
    public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {
        if (remoteData != null && !remoteData.isEmpty()) {
            localDataSource.updateHistoryFromRemote(remoteData);
        }
    }

    @Override public void onFailureFromLocal(Exception e) { historyList.postValue(new Result.Error(e.getMessage())); }
    @Override public void onFailureFromRemote(Exception e) { Log.w(TAG, e.getMessage()); }
    @Override public void onSuccessSaveLocal() { }
}