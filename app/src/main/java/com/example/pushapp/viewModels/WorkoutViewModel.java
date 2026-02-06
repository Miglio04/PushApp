package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.api.ExerciseApiModel;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository; // --- NUOVO ---
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.utils.SessionManager; // --- NUOVO ---

import java.util.List;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    // --- REPOSITORIES ---
    private final ExerciseRepository exerciseRepository;
    private final TrainingRepository trainingRepository;
    private final HistoryRepository historyRepository; // --- NUOVO ---
    private final SessionManager sessionManager;       // --- NUOVO ---

    // --- TRAINING PADRE ---
    private Training parentTraining;

    // --- CAMPI TIMER ---
    private long startTime = 0L;
    private long timeWhenPaused = 0L;
    private long restEndTime = 0L;
    // --- NUOVO: Serve per il salvataggio dello storico (Data reale di inizio) ---
    private long sessionStartTimeMillis = 0L;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    // --- LIVE DATA PER LO STATO DELL'ALLENAMENTO ---
    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<Routine> activeTrainingDay = new MutableLiveData<>();

    // --- LIVE DATA PER LA NAVIGAZIONE (CHIUSURA SCHERMATA) ---
    private final MutableLiveData<Boolean> navigateToHome = new MutableLiveData<>(false); // --- NUOVO ---

    // --- LIVE DATA PER IL TIMER PRINCIPALE ---
    private final MutableLiveData<Boolean> isWorkoutTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> formattedTime = new MutableLiveData<>("00:00");
    private final MutableLiveData<Long> elapsedMillis = new MutableLiveData<>(0L);

    // --- LIVE DATA PER IL TIMER DI RIPOSO ---
    private final MutableLiveData<Boolean> isRestTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> restSecondsRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> restTotalSeconds = new MutableLiveData<>(0);

    // --- LIVE DATA PER IL CATALOGO ESERCIZI (API) ---
    private final MutableLiveData<List<ExerciseApiModel>> availableExercises = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // --- COSTRUTTORE AGGIORNATO ---
    public WorkoutViewModel(TrainingRepository trainingRepository,
                            ExerciseRepository exerciseRepository,
                            HistoryRepository historyRepository, // --- NUOVO ---
                            SessionManager sessionManager) {     // --- NUOVO ---
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.historyRepository = historyRepository;
        this.sessionManager = sessionManager;
    }

    // --- GETTERS (Aggiunto navigateToHome) ---
    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<Routine> getActiveTrainingDay() { return activeTrainingDay; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<List<ExerciseApiModel>> getAvailableExercises() { return availableExercises; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; } // --- NUOVO ---

    // --- LOGICA DI CONTROLLO PRINCIPALE ---

    public void startWorkout(Routine day, Training parentTraining) {
        if (day == null) return;
        this.parentTraining = parentTraining;
        this.sessionStartTimeMillis = System.currentTimeMillis(); // Salviamo l'orario reale

        workoutTitle.setValue(day.getName());
        activeTrainingDay.setValue(day);
        isWorkoutInProgress.setValue(true);

        resetWorkoutTimer();
        startWorkoutTimer();

        // --- NUOVO: Salvataggio immediato anti-crash ---
        sessionManager.saveSessionState(day, sessionStartTimeMillis);
    }

    // --- NUOVO: Sostituisce il vecchio stopWorkout ---
    public void finishWorkout() {
        Routine currentRoutine = activeTrainingDay.getValue();
        if (currentRoutine == null) return;

        pauseWorkoutTimer(); // Fermiamo tutto visivamente

        // 1. Chiamiamo il Repository per salvare (Room + Firebase)
        historyRepository.saveWorkout(currentRoutine, sessionStartTimeMillis, () -> {
            // 2. Callback di successo: Puliamo tutto
            sessionManager.clearSession(); // Rimuoviamo il backup temporaneo
            resetWorkoutState();

            // 3. Avvisiamo la UI di chiudersi
            navigateToHome.postValue(true);
        });
    }

    // --- NUOVO: Metodo per annullare senza salvare ---
    public void cancelWorkout() {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true);
    }

    // --- NUOVO: Controllo all'avvio (da chiamare in MainActivity) ---
    public void checkRestoredSession() {
        if (sessionManager.isSessionActive()) {
            Routine savedRoutine = sessionManager.getSavedRoutine();
            long savedStartTime = sessionManager.getSavedStartTime();

            if (savedRoutine != null) {
                // Ripristiniamo lo stato
                activeTrainingDay.setValue(savedRoutine);
                workoutTitle.setValue(savedRoutine.getName());
                isWorkoutInProgress.setValue(true);
                sessionStartTimeMillis = savedStartTime;

                // Calcoliamo il tempo trascorso
                long now = System.currentTimeMillis();
                long elapsed = now - savedStartTime;

                // Facciamo ripartire il timer da dove era rimasto
                startTime = SystemClock.elapsedRealtime() - elapsed;
                timeWhenPaused = elapsed; // Trucco per far ripartire il runnable correttamente

                isWorkoutTimerRunning.setValue(true);
                timerHandler.post(updateRunnable);
            }
        }
    }

    private void resetWorkoutState() {
        parentTraining = null;
        workoutTitle.setValue(null);
        activeTrainingDay.setValue(null);
        isWorkoutInProgress.setValue(false);
        navigateToHome.setValue(false); // Reset navigazione
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
    }

    // --- MODIFICA: Aggiorniamo lo stato su disco ad ogni cambiamento ---
    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        Routine currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getWorkoutExercises() == null) return;
        List<WorkoutExercise> workoutExercises = currentDay.getWorkoutExercises();
        if (exercisePosition >= 0 && exercisePosition < workoutExercises.size()) {
            WorkoutExercise workoutExercise = workoutExercises.get(exercisePosition);
            if (workoutExercise.getSeries() != null && setPosition >= 0 && setPosition < workoutExercise.getSeries().size()) {
                Serie serie = workoutExercise.getSeries().get(setPosition);
                boolean newState = !serie.isCompleted();
                serie.setCompleted(newState);

                if (newState) {
                    startRestTimer(restTimeSeconds);
                } else {
                    stopRestTimer();
                }
                activeTrainingDay.setValue(currentDay);

                // --- NUOVO: Salviamo lo stato aggiornato ---
                sessionManager.saveSessionState(currentDay, sessionStartTimeMillis);
            }
        }
    }

    // --- MODIFICA: Aggiorniamo lo stato su disco ad ogni cambiamento ---
    public void addSetToExercise(int exercisePosition) {
        Routine currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getWorkoutExercises() == null) return;

        List<WorkoutExercise> workoutExercises = currentDay.getWorkoutExercises();
        if (exercisePosition >= 0 && exercisePosition < workoutExercises.size()) {
            WorkoutExercise workoutExercise = workoutExercises.get(exercisePosition);
            List<Serie> series = workoutExercise.getSeries();

            if (series == null) {
                series = new java.util.ArrayList<>();
                workoutExercise.setSeries(series);
            }

            Serie newSerie = new Serie();
            newSerie.setSerieNumber(series.size() + 1);

            if (!series.isEmpty()) {
                Serie lastSerie = series.get(series.size() - 1);
                newSerie.setTargetWeight(lastSerie.getTargetWeight());
                newSerie.setTargetReps(lastSerie.getTargetReps());
            } else {
                newSerie.setTargetWeight(0);
                newSerie.setTargetReps(10);
            }

            series.add(newSerie);
            activeTrainingDay.setValue(currentDay);

            // --- NUOVO: Salvataggio ---
            sessionManager.saveSessionState(currentDay, sessionStartTimeMillis);
        }
    }

    // --- MODIFICA: Aggiorniamo lo stato su disco ad ogni cambiamento ---
    public void updateSetData(int exercisePosition, int setPosition, double actualWeight, int actualReps) {
        Routine currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getWorkoutExercises() == null) return;

        List<WorkoutExercise> workoutExercises = currentDay.getWorkoutExercises();
        if (exercisePosition >= 0 && exercisePosition < workoutExercises.size()) {
            WorkoutExercise workoutExercise = workoutExercises.get(exercisePosition);
            if (workoutExercise.getSeries() != null && setPosition >= 0 && setPosition < workoutExercise.getSeries().size()) {
                Serie serie = workoutExercise.getSeries().get(setPosition);

                serie.setActualWeight(actualWeight);
                serie.setActualReps(actualReps);

                // --- NUOVO: Salvataggio (Attenzione: potresti volerlo fare solo se l'utente smette di scrivere per 1 secondo per performance, ma per ora va bene così)
                sessionManager.saveSessionState(currentDay, sessionStartTimeMillis);
            }
        }
    }

    // --- MODIFICA: Aggiorniamo lo stato su disco ad ogni cambiamento ---
    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        Routine currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getWorkoutExercises() == null) return;

        List<WorkoutExercise> workoutExercises = currentDay.getWorkoutExercises();
        if (exercisePosition >= 0 && exercisePosition < workoutExercises.size()) {
            WorkoutExercise workoutExercise = workoutExercises.get(exercisePosition);
            List<Serie> series = workoutExercise.getSeries();

            if (series != null && setPosition >= 0 && setPosition < series.size()) {
                series.remove(setPosition);

                for (int i = 0; i < series.size(); i++) {
                    series.get(i).setSerieNumber(i + 1);
                }

                activeTrainingDay.setValue(currentDay);

                // --- NUOVO: Salvataggio ---
                sessionManager.saveSessionState(currentDay, sessionStartTimeMillis);
            }
        }
    }

    // --- LOGICA DI RETE (API) - INVARIATA ---
    public void loadAvailableExercises() {
        if (availableExercises.getValue() != null && !availableExercises.getValue().isEmpty()) {
            return;
        }
        exerciseRepository.getAvailableExercises(new FirebaseCallback<List<ExerciseApiModel>>() {
            @Override
            public void onSuccess(List<ExerciseApiModel> result) {
                availableExercises.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("API Error: " + e.getMessage());
            }
        });
    }

    // --- METODI DEI TIMER (INVARIATI) ---
    public void startWorkoutTimer() {
        if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) return;
        // Se non è un ripristino da crash (startTime == 0), inizializza
        if (startTime == 0) {
            startTime = SystemClock.elapsedRealtime() - timeWhenPaused;
        }
        isWorkoutTimerRunning.setValue(true);
        timerHandler.post(updateRunnable);
    }

    public void pauseWorkoutTimer() {
        if (Boolean.FALSE.equals(isWorkoutTimerRunning.getValue())) return;
        timeWhenPaused = elapsedMillis.getValue() != null ? elapsedMillis.getValue() : 0L;
        isWorkoutTimerRunning.setValue(false);
        timerHandler.removeCallbacks(updateRunnable);
    }

    public void startRestTimer(int seconds) {
        stopRestTimer();
        restTotalSeconds.setValue(seconds);
        restSecondsRemaining.setValue(seconds);
        restEndTime = SystemClock.elapsedRealtime() + (seconds * 1000L);
        isRestTimerRunning.setValue(true);
        timerHandler.post(restUpdateRunnable);
    }

    public void stopRestTimer() {
        timerHandler.removeCallbacks(restUpdateRunnable);
        isRestTimerRunning.setValue(false);
        restSecondsRemaining.setValue(0);
    }

    public void skipRestTimer() {
        stopRestTimer();
    }

    // --- RUNNABLES (INVARIATI) ---
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) {
                long now = SystemClock.elapsedRealtime();
                long totalMillis = now - startTime; // Calcolo diretto
                elapsedMillis.setValue(totalMillis);
                formattedTime.setValue(formatMillis(totalMillis));
                timerHandler.postDelayed(this, 100L);
            }
        }
    };
    private final Runnable restUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (Boolean.TRUE.equals(isRestTimerRunning.getValue())) {
                long now = SystemClock.elapsedRealtime();
                long remaining = restEndTime - now;

                if (remaining <= 0) {
                    restSecondsRemaining.setValue(0);
                    isRestTimerRunning.setValue(false);
                } else {
                    restSecondsRemaining.setValue((int) (remaining / 1000));
                    timerHandler.postDelayed(this, 100L);
                }
            }
        }
    };

    // --- UTILITY (INVARIATE) ---
    private String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
    private void resetWorkoutTimer() {
        startTime = 0L;
        timeWhenPaused = 0L;
        elapsedMillis.setValue(0L);
        formattedTime.setValue("00:00");
    }
}