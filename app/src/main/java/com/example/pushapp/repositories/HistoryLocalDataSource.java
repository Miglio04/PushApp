package com.example.pushapp.repositories;

import com.example.pushapp.database.HistoryDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import java.util.List;

/**
 * Data source for handling history-related operations with the local Room database.
 * Executes database operations regarding workout sessions, exercises, and series
 * asynchronously on a background thread.
 */
public class HistoryLocalDataSource {

    private final HistoryDao historyDao;
    private HistoryCallback historyCallback;

    HistoryLocalDataSource(LocalDatabase database) {
        this.historyDao = database.historyDao();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param callback The callback implementation.
     */
    public void setHistoryCallback(HistoryCallback callback) {
        this.historyCallback = callback;
    }

    /**
     * Saves a complete workout session (session info, exercises, series) to the local database.
     *
     * @param session   The session entity.
     * @param exercises The list of exercises in the session.
     * @param series    The list of series in the session.
     * @param callback  Callback to notify upon completion or failure.
     */
    public void saveSession(HistorySession session, List<HistoryWorkoutExercise> exercises, List<HistorySerie> series, HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                historyDao.insertSession(session);

                for (HistoryWorkoutExercise exercise : exercises) {
                    historyDao.insertWorkoutExercise(exercise);
                }

                for (HistorySerie serie : series) {
                    historyDao.insertSerie(serie);
                }

                if (callback != null) {
                    callback.onSuccessSaveLocal();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailureFromLocal(e);
                }
            }
        });
    }

    /**
     * Retrieves all workout history sessions from the local database.
     * Results are delivered via the registered HistoryCallback.
     */
    public void getAllHistory() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<HistorySessionWithExercises> result = historyDao.getAllHistory();
                if (historyCallback != null) {
                    historyCallback.onSuccessHistoryListFromLocal(result);
                }
            } catch (Exception e) {
                if (historyCallback != null) {
                    historyCallback.onFailureFromLocal(e);
                }
            }
        });
    }

    /**
     * Calculates statistics for a specific exercise to generate graph data.
     *
     * @param exerciseName The name of the exercise.
     * @param metric       The metric to calculate (e.g., Max Weight, Total Volume).
     * @param callback     Callback to receive the list of graph points.
     */
    public void getGraphData(String exerciseName, HistoryRepository.StatMetric metric, HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<GraphPoint> points;
                switch (metric) {
                    case TOTAL_VOLUME:
                        points = historyDao.getTotalVolumeStats(exerciseName);
                        break;
                    case ESTIMATED_1RM:
                        points = historyDao.getOneRepMaxStats(exerciseName);
                        break;
                    case MAX_WEIGHT:
                    default:
                        points = historyDao.getMaxWeightStats(exerciseName);
                        break;
                }

                if (callback != null) {
                    callback.onSuccessGraphDataFromLocal(points);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailureFromLocal(e);
                }
            }
        });
    }

    /**
     * Deletes a specific workout session from the local database.
     *
     * @param sessionId The ID of the session to delete.
     * @param onSuccess Runnable to execute upon successful deletion.
     */
    public void deleteSession(String sessionId, Runnable onSuccess) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                historyDao.deleteSessionById(sessionId);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {}
        });
    }

    /**
     * Updates the local database with a list of history sessions fetched from a remote source.
     * Inserts or replaces local records with the provided remote data.
     *
     * @param remoteData The list of history sessions from the remote source.
     */
    public void updateHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                for (HistorySessionWithExercises item : remoteData) {
                    historyDao.insertSession(item.session);

                    for (var exWithSeries : item.exercises) {
                        historyDao.insertWorkoutExercise(exWithSeries.historyWorkoutExercise);

                        for (HistorySerie serie : exWithSeries.historySeries) {
                            historyDao.insertSerie(serie);
                        }
                    }
                }
                List<HistorySessionWithExercises> updatedList = historyDao.getAllHistory();
                if (historyCallback != null) {
                    historyCallback.onSuccessHistoryListFromLocal(updatedList);
                }
            } catch (Exception e) {
                if (historyCallback != null) {
                    historyCallback.onFailureFromLocal(e);
                }
            }
        });
    }

    /**
     * Clears all history data (sessions, exercises, series) from the local database.
     */
    public void resetLocalDatabase() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                historyDao.deleteAllHistory();
            } catch (Exception e) {}
        });
    }
}