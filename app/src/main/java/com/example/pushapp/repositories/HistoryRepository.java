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
import com.example.pushapp.utils.WorkoutState;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository implements HistoryCallback {
    private final String TAG = "HistoryRepository";
    private final HistoryLocalDataSource localDataSource;
    private final HistoryRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> historyList = new MutableLiveData<>();
    private final MutableLiveData<Result> graphData = new MutableLiveData<>();
    private final MutableLiveData<Result> graphVolumeData = new MutableLiveData<>();

    public enum StatMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }

    HistoryRepository(HistoryLocalDataSource localDataSource, HistoryRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setHistoryCallback(this);
        if (this.remoteDataSource != null) {
            this.remoteDataSource.setHistoryCallback(this);
        }
    }

    public LiveData<Result> getHistoryList() {
        return historyList;
    }

    public void fetchHistoryData() {
        localDataSource.getAllHistory();
        if (remoteDataSource != null) {
            remoteDataSource.fetchHistoryFromRemote();
        }
    }


    public void searchHistory(String query) {
        if (query == null || query.isEmpty()) {
            localDataSource.getAllHistory();
        } else {
            localDataSource.searchHistory(query);
        }
    }

    public HistorySessionWithExercises createNewWorkoutSessionWithouthTemplate(Routine day) {
        if (day == null) return null;

        String currentUserId = day.getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot create workout session: Routine template does not have a userId.");
            return null;
        }

        long workoutStartTimeMillis = System.currentTimeMillis();

        HistorySession newSession = new HistorySession(day.getName(), workoutStartTimeMillis, 0);
        newSession.setUserId(currentUserId);

        List<HistoryWorkoutExerciseWithSeries> historyExercises = new ArrayList<>();
        for (WorkoutExercise woEx : day.getWorkoutExercises()) {

            HistoryWorkoutExercise hExercise = new HistoryWorkoutExercise(
                    newSession.getHistorySessionId(),
                    woEx.getApiExerciseId(),
                    day.getWorkoutExercises().indexOf(woEx)
            );
            hExercise.setUserId(currentUserId);

            List<HistorySerie> historySeries = new ArrayList<>();
            for (Serie templateSerie : woEx.getSeries()) {
                HistorySerie hSerie = new HistorySerie(
                        hExercise.getHistoryExerciseId(),
                        templateSerie.getSerieNumber(),
                        0,
                        0
                );
                hSerie.setUserId(currentUserId);
                historySeries.add(hSerie);
            }

            hExercise.setCurrentRestTimeIndex(woEx.getRestTimeIndex());
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

    public WorkoutState createNewWorkoutSessionWithTemplate(Routine day) {
        HistorySessionWithExercises session = createNewWorkoutSessionWithouthTemplate(day);
        if (session == null) return null;
        return new WorkoutState(session, day);
    }

    public void saveWorkoutSession(HistorySessionWithExercises sessionToSave, Runnable onComplete) {
        if (sessionToSave == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        if (sessionToSave.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries ex : sessionToSave.exercises) {
                if (ex.historySeries != null) {
                    ex.historySeries.removeIf(serie -> serie.getReps() == 0);
                }
            }
            sessionToSave.exercises.removeIf(ex -> ex.historySeries == null || ex.historySeries.isEmpty());
        }

        if (sessionToSave.exercises == null || sessionToSave.exercises.isEmpty()) {
            Log.i(TAG, "Workout session is empty after cleanup. Aborting save.");
            if (onComplete != null) {
                onComplete.run();
            }
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
                    remoteDataSource.uploadWorkoutSession(sessionToSave, HistoryRepository.this);
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
        localDataSource.deleteSession(sessionId, () -> {
            localDataSource.getAllHistory();
        });

        if (remoteDataSource != null) {
            remoteDataSource.deleteSession(sessionId);
        }
    }

    public LiveData<Result> getGraphData() {
        return graphData;
    }

    public LiveData<Result> getGraphVolumeData() {
        return graphVolumeData;
    }

    public void fetchGraphData(String exerciseName, StatMetric metric) {
        localDataSource.getGraphData(exerciseName, metric, new HistoryCallback() {
            @Override
            public void onSuccessGraphDataFromLocal(List<GraphPoint> points) {
                if (metric == StatMetric.TOTAL_VOLUME) {
                    graphVolumeData.postValue(new Result.GraphSuccess(points));
                } else {
                    graphData.postValue(new Result.GraphSuccess(points));
                }
            }
            @Override public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list) {}
            @Override public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {}
            @Override public void onSuccessSaveLocal() {}
            @Override public void onFailureFromLocal(Exception e) {}
            @Override public void onFailureFromRemote(Exception e) {}
        });
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
    @Override public void onFailureFromRemote(Exception e) { Log.w(TAG, "Firebase upload failed: " + e.getMessage()); }
    @Override public void onSuccessSaveLocal() { }

    public void resetLocalDatabase() {
        localDataSource.resetLocalDatabase();
        if(historyList != null) historyList.postValue(null);
        if(graphData != null) graphData.postValue(null);
        if(graphVolumeData != null) graphVolumeData.postValue(null);
    }
}
