package com.example.pushapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

/**
 * Repository class responsible for managing workout history data.
 * Mediate between local (Room) and remote (Firestore) data sources to fetch, save,
 * and synchronize workout sessions and statistics.
 */
public class HistoryRepository implements HistoryCallback {
    private final HistoryLocalDataSource localDataSource;
    private final HistoryRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> historyList = new MutableLiveData<>();

    /**
     * Enum representing the different types of statistics metrics that can be queried.
     */
    public enum StatMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }

    /**
     * Constructs a new HistoryRepository.
     * Initializes data sources and sets this repository as the callback listener.
     *
     * @param localDataSource  The local data source for history.
     * @param remoteDataSource The remote data source for history.
     */
    public HistoryRepository(HistoryLocalDataSource localDataSource, HistoryRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setHistoryCallback(this);
        if (this.remoteDataSource != null) {
            this.remoteDataSource.setHistoryCallback(this);
        }
    }

    /**
     * Returns the LiveData observing the history list.
     *
     * @return LiveData containing the Result of history operations.
     */
    public LiveData<Result> getHistoryList() {
        return historyList;
    }

    /**
     * Triggers a fetch of history data.
     * Calls the local data source immediately and optionally synchronizes with the remote source.
     */
    public void fetchHistoryData() {
        localDataSource.getAllHistory();
        if (remoteDataSource != null) {
            remoteDataSource.fetchHistoryFromRemote();
        }
    }


    /**
     * Creates a new workout session object from a Routine template without saving it to the database yet.
     * Used to initialize a workout session state.
     *
     * @param day The Routine template to base the session on.
     * @return A populated HistorySessionWithExercises object, or null if the routine is invalid.
     */
    public HistorySessionWithExercises createNewWorkoutSessionWithoutTemplate(Routine day) {
        if (day == null) return null;

        String currentUserId = day.getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            return null;
        }

        long workoutStartTimeMillis = System.currentTimeMillis();

        HistorySession newSession = new HistorySession(day.getName(), workoutStartTimeMillis, 0);
        newSession.setUserId(currentUserId);

        List<HistoryWorkoutExerciseWithSeries> historyExercises = new ArrayList<>();
        for (WorkoutExercise woEx : day.getWorkoutExercises()) {

            HistoryWorkoutExercise hExercise = new HistoryWorkoutExercise(
                    newSession.getHistorySessionId(),
                    woEx.getExerciseName(),
                    day.getWorkoutExercises().indexOf(woEx)
            );
            hExercise.setUserId(currentUserId);

            List<HistorySerie> historySeries = new ArrayList<>();
            int count = 1;
            for (Serie ignored : woEx.getSeries()) {
                HistorySerie hSeries = new HistorySerie(
                        hExercise.getHistoryExerciseId(),
                        count,
                        0,
                        0
                );
                hSeries.setUserId(currentUserId);
                historySeries.add(hSeries);
                count++;
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

    /**
     * Saves a completed workout session to the local database.
     * Upon local success, attempts to upload the session to the remote server.
     *
     * @param sessionToSave The session object to save.
     * @param onComplete    A Runnable to execute when the local save is complete.
     */
    public void saveWorkoutSession(HistorySessionWithExercises sessionToSave, Runnable onComplete) {
        if (sessionToSave == null) {
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
        });
    }

    /**
     * Deletes a specific workout session locally and remotely.
     *
     * @param sessionId The ID of the session to delete.
     */
    public void deleteSession(String sessionId) {
        localDataSource.deleteSession(sessionId, localDataSource::getAllHistory);

        if (remoteDataSource != null) {
            remoteDataSource.deleteSession(sessionId, e -> {}
            );
        }
    }

    /**
     * Fetches graph data for a specific exercise and metric.
     *
     * @param exerciseName The name of the exercise.
     * @param metric       The metric to calculate (e.g., Max Weight, Volume).
     * @param callback     The callback to receive the graph data points.
     */
    public void fetchGraphData(String exerciseName, StatMetric metric, HistoryCallback callback) {
        localDataSource.getGraphData(exerciseName, metric, callback);
    }

    /**
     * Callback received when the history list is successfully retrieved from local storage.
     * Updates the LiveData.
     *
     * @param list The list of history sessions.
     */
    @Override
    public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list) {
        historyList.postValue(new Result.HistorySuccess(list));
    }

    /**
     * Callback received when history data is fetched from the remote source.
     * Updates the local database with the remote data.
     *
     * @param remoteData The list of history sessions from the server.
     */
    @Override
    public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {
        if (remoteData != null && !remoteData.isEmpty()) {
            localDataSource.updateHistoryFromRemote(remoteData);
        }
    }

    /**
     * Callback received when a local database operation fails.
     * Posts an error to the LiveData.
     *
     * @param e The exception causing the failure.
     */
    @Override public void onFailureFromLocal(Exception e) {
        historyList.postValue(new Result.Error(e.getMessage()));
    }

    /**
     * Callback received when a remote operation fails.
     * Posts an error to the LiveData.
     *
     * @param e The exception causing the failure.
     */
    @Override public void onFailureFromRemote(Exception e) {
        historyList.postValue(new Result.Error(e.getMessage()));
    }

    /**
     * Callback received when a local save operation completes successfully.
     * Refreshes the history list.
     */
    @Override public void onSuccessSaveLocal() {
        localDataSource.getAllHistory();
    }

    /**
     * Resets the local database by clearing all history data.
     */
    public void resetLocalDatabase() {
        localDataSource.resetLocalDatabase();
        historyList.postValue(null);
    }
}
