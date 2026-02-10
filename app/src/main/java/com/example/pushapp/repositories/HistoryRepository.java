package com.example.pushapp.repositories;

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

    private static volatile HistoryRepository INSTANCE;

    private final HistoryLocalDataSource localDataSource;
    private final HistoryRemoteDataSource remoteDataSource;

    private final MutableLiveData<Result> historyListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result> graphDataLiveData = new MutableLiveData<>();

    // Enum per i tipi di statistiche grafiche
    public enum StatMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }

    // Costruttore privato (Singleton)
    private HistoryRepository(HistoryLocalDataSource localDataSource, HistoryRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;

        // Imposta il repository come ascoltatore degli eventi remoti
        if (this.remoteDataSource != null) {
            this.remoteDataSource.setCallback(this);
        }
    }

    public static HistoryRepository getInstance(HistoryLocalDataSource localDataSource, HistoryRemoteDataSource remoteDataSource) {
        if (INSTANCE == null) {
            synchronized (HistoryRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HistoryRepository(localDataSource, remoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    // =================================================================================
    //  CASO D'USO 1: SAVE WORKOUT (Salvataggio + Upload)
    // =================================================================================
    public void saveWorkout(Routine activeRoutine, long startTime, Runnable onComplete) {
        // 1. Creazione ID univoci e MAPPING (Routine -> History)
        String sessionId = UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis();

        // Creazione Oggetto Sessione
        HistorySession session = new HistorySession(sessionId, activeRoutine.getName(), startTime, endTime);

        List<HistoryWorkoutExercise> historyExercises = new ArrayList<>();
        List<HistorySerie> historySeries = new ArrayList<>();

        // Iterazione Esercizi
        for (WorkoutExercise workoutExercise : activeRoutine.getWorkoutExercises()) {
            String historyExerciseId = UUID.randomUUID().toString();

            HistoryWorkoutExercise hExercise = new HistoryWorkoutExercise(
                    historyExerciseId,
                    sessionId,
                    workoutExercise.getApiExerciseId(),
                    activeRoutine.getWorkoutExercises().indexOf(workoutExercise) // Ordine
            );
            historyExercises.add(hExercise);

            // Iterazione Serie
            for (Serie s : workoutExercise.getSeries()) {
                // Salviamo solo le serie completate!
                // Temporaneo, si basa su architettura vecchia
                /*if (s.isCompleted()) {
                    HistorySerie hSerie = new HistorySerie(
                            UUID.randomUUID().toString(),
                            historyExerciseId,
                            workoutExercise.getSeries().indexOf(s) + 1, // Numero serie (1-based)
                            s.getActualWeight(),
                            s.getActualReps()
                    );
                    historySeries.add(hSerie);
                }

                 */
            }
        }

        // 2. Salvataggio Locale (Passiamo anche il callback per sapere quando ha finito)
        localDataSource.saveSession(session, historyExercises, historySeries, new HistoryCallback() {
            @Override
            public void onSuccessSaveLocal() {
                // 3. Se locale OK -> Upload su Firebase (Fire & Forget)
                if (remoteDataSource != null) {
                    remoteDataSource.uploadSession(session, historyExercises, historySeries);
                }
                // Avvisiamo la UI che abbiamo finito
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onFailureFromLocal(Exception e) {
                // Gestione errore salvataggio locale
                e.printStackTrace();
            }

            // Metodi non usati in questo contesto specifico
            @Override public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> list) {}
            @Override public void onSuccessGraphDataFromLocal(List<GraphPoint> points) {}
            @Override public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> list) {}
            @Override public void onFailureFromRemote(Exception e) {}
        });
    }

    // =================================================================================
    //  CASO D'USO 2: GET HISTORY LIST (Sync)
    // =================================================================================
    public LiveData<Result> getHistoryList() {
        // 1. Chiede subito i dati locali (Cache Veloce)
        localDataSource.getAllHistory(this);

        // 2. Lancia il fetch remoto (Sync Lento)
        if (remoteDataSource != null) {
            remoteDataSource.fetchHistoryFromRemote();
        }

        return historyListLiveData;
    }

    // =================================================================================
    //  CASO D'USO 3: SEARCH HISTORY (Filtro Locale)
    // =================================================================================
    public void searchHistory(String exerciseName) {
        // Chiede al local di filtrare. Il risultato tornerà nel metodo onSuccessHistoryListFromLocal
        // che aggiornerà historyListLiveData.
        localDataSource.searchHistoryByExercise(exerciseName, this);
    }

    // =================================================================================
    //  CASO D'USO 4: GET GRAPH DATA (Aggregazione)
    // =================================================================================
    public LiveData<Result> getGraphData(String exerciseName, StatMetric metric) {
        // Chiede i punti al locale.
        localDataSource.getGraphData(exerciseName, metric, this);
        return graphDataLiveData;
    }

    // =================================================================================
    //  CASO D'USO 5: DELETE SESSION
    // =================================================================================
    public void deleteSession(String sessionId) {
        // Cancella dal locale
        localDataSource.deleteSession(sessionId);

        // Cancella dal remoto
        if (remoteDataSource != null) {
            remoteDataSource.deleteSession(sessionId);
        }

        // Ricarica la lista per aggiornare la UI
        getHistoryList();
    }

    // =================================================================================
    //  CALLBACK IMPLEMENTATION (Risposte dai DataSource)
    // =================================================================================

    @Override
    public void onSuccessHistoryListFromLocal(List<HistorySessionWithExercises> historyList) {
        // Aggiorna il LiveData con i dati locali (che arrivano sia da getAllHistory sia da searchHistory)
        historyListLiveData.postValue(new Result.HistorySuccess(historyList));
    }

    @Override
    public void onSuccessGraphDataFromLocal(List<GraphPoint> points) {
        graphDataLiveData.postValue(new Result.GraphSuccess(points));
    }

    @Override
    public void onSuccessHistoryFromRemote(List<HistorySessionWithExercises> remoteHistory) {
        // Abbiamo ricevuto dati nuovi da Firebase
        // Ora dobbiamo salvarli nel DB Locale per aggiornare la "Single Source of Truth".
        // Il LocalDataSource si occuperà di sovrascrivere/unire.
        if (remoteHistory != null && !remoteHistory.isEmpty()) {
            localDataSource.updateHistoryFromRemote(remoteHistory, this);
        }
    }

    @Override
    public void onFailureFromLocal(Exception e) {
        historyListLiveData.postValue(new Result.Error(e));
    }

    @Override
    public void onFailureFromRemote(Exception e) {
        // Loggare l'errore, ma non disturbare l'utente se ha i dati locali
        System.err.println("Remote Sync Failed: " + e.getMessage());
    }

    @Override
    public void onSuccessSaveLocal() {
        // Usato internamente nel metodo saveWorkout
    }
}