package com.example.pushapp.repositories;

import android.util.Log;
import com.example.pushapp.database.HistoryDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import java.util.List;

public class HistoryLocalDataSource {

    private final HistoryDao historyDao;
    private HistoryCallback historyCallback;

    public HistoryLocalDataSource(LocalDatabase database) {
        this.historyDao = database.historyDao();
    }

    public void setHistoryCallback(HistoryCallback callback) {
        this.historyCallback = callback;
    }

    public void searchHistory(String query) {
        searchHistory(query, this.historyCallback);
    }

    public void searchHistory(String query, HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<HistorySessionWithExercises> result = historyDao.searchHistory(query);
                if (callback != null) {
                    callback.onSuccessHistoryListFromLocal(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailureFromLocal(e);
                }
            }
        });
    }

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

    public void getAllHistory() {
        getAllHistory(this.historyCallback);
    }

    public void getAllHistory(HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<HistorySessionWithExercises> result = historyDao.getAllHistory();
                if (callback != null) {
                    callback.onSuccessHistoryListFromLocal(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailureFromLocal(e);
                }
            }
        });
    }

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

    public void deleteSession(String sessionId, Runnable onSuccess) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                historyDao.deleteSessionById(sessionId);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                Log.e("HistoryLocalDataSrc", "Failed to delete session: " + e.getMessage(), e);
            }
        });
    }

    public void updateHistoryFromRemote(List<HistorySessionWithExercises> remoteData) {
        updateHistoryFromRemote(remoteData, this.historyCallback);
    }

    public void updateHistoryFromRemote(List<HistorySessionWithExercises> remoteData, HistoryCallback callback) {
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
                if (callback != null) {
                    callback.onSuccessHistoryListFromLocal(updatedList);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailureFromLocal(e);
                }
            }
        });
    }
}