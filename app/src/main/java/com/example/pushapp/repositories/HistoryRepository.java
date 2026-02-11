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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void saveWorkout(Routine activeRoutine, long startTime, Runnable onComplete) {
        final String sessionId = UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis();

        // Calcoliamo il volume totale della sessione per le statistiche veloci
        double totalVolume = 0;

        final HistorySession session = new HistorySession(sessionId, activeRoutine.getName(), startTime, endTime);
        final List<HistoryWorkoutExercise> hExercises = new ArrayList<>();
        final List<HistorySerie> hSeries = new ArrayList<>();

        for (WorkoutExercise workoutExercise : activeRoutine.getWorkoutExercises()) {
            String historyExerciseId = UUID.randomUUID().toString();

            HistoryWorkoutExercise hExercise = new HistoryWorkoutExercise(
                    historyExerciseId,
                    sessionId,
                    workoutExercise.getApiExerciseId(), // Assicurati che questo ID esista nel modello WorkoutExercise
                    activeRoutine.getWorkoutExercises().indexOf(workoutExercise)
            );
            hExercises.add(hExercise);

            for (Serie s : workoutExercise.getSeries()) {
                // Salviamo SOLO le serie completate
                if (s.isCompleted()) {
                    HistorySerie hSerie = new HistorySerie(
                            UUID.randomUUID().toString(),
                            historyExerciseId,
                            workoutExercise.getSeries().indexOf(s) + 1,
                            s.getActualWeight(),
                            s.getActualReps()
                    );
                    hSeries.add(hSerie);
                }
            }
        }
        /*localDataSource.saveSession(session, hExercises, hSeries, new HistoryCallback() {
            @Override
            public void onSuccessSaveLocal() {
                // Se il locale ha successo, proviamo l'upload remoto se disponibile
                if (remoteDataSource != null) {
                    remoteDataSource.uploadSession(session, hExercises, hSeries);
                }
                // Aggiorniamo la lista LiveData
                localDataSource.getAllHistory();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailureFromLocal(Exception e) {
                historyList.postValue(new Result.Error(e.getMessage()));
            }

            // Metodi vuoti obbligatori dall'interfaccia
            @Override public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> l) {}
            @Override public void onSuccessGraphDataFromLocal(List<GraphPoint> p) {}
            @Override public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> l) {}
            @Override public void onFailureFromRemote(Exception e) {}
        });
         */
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

    @Override public void onFailureFromLocal(Exception e) {
        historyList.postValue(new Result.Error(e.getMessage()));
    }

    @Override public void onFailureFromRemote(Exception e) {
        Log.w(TAG, e.getMessage());
    }

    @Override public void onSuccessSaveLocal() { }
}