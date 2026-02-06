package com.example.pushapp.repositories;

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

    // --- MODIFICA QUI: Il costruttore ora accetta solo il DAO ---
    public HistoryLocalDataSource(HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    // --- SALVATAGGIO ---
    public void saveSession(HistorySession session, List<HistoryWorkoutExercise> exercises, List<HistorySerie> series, HistoryCallback callback) {
        // Nota: databaseWriteExecutor è statico, quindi possiamo chiamarlo anche senza avere l'istanza del database nel costruttore
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Eseguiamo tutto in una transazione implicita (insert sequenziali)
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

    // --- LETTURA STORICO COMPLETO ---
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

    // --- RICERCA (FILTRO) ---
    public void searchHistoryByExercise(String exerciseName, HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<HistorySessionWithExercises> result = historyDao.getHistoryByExercise(exerciseName);
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

    // --- GRAFICI ---
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

    // --- ELIMINAZIONE ---
    public void deleteSession(String sessionId) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                historyDao.deleteSessionById(sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // --- AGGIORNAMENTO DA REMOTO (SYNC) ---
    public void updateHistoryFromRemote(List<HistorySessionWithExercises> remoteData, HistoryCallback callback) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                for (HistorySessionWithExercises item : remoteData) {
                    // 1. Inserisci Sessione
                    historyDao.insertSession(item.session);

                    // 2. Inserisci Esercizi e relative Serie
                    for (var exWithSeries : item.exercises) {
                        historyDao.insertWorkoutExercise(exWithSeries.historyWorkoutExercise);

                        for (HistorySerie serie : exWithSeries.historySeries) {
                            historyDao.insertSerie(serie);
                        }
                    }
                }
                // Dopo aver salvato tutto, ricarichiamo la lista aggiornata per la UI
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