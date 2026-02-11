package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.repositories.TrainingRepository;
// --- FIX 1: Import corretto (utils) ---
import com.example.pushapp.utils.SessionManager;

import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    private final String TAG = "WorkoutViewModel";
    private final ExerciseRepository exerciseRepository;
    private final TrainingRepository trainingRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;

    private Training parentTraining;
    private long workoutStartTimeMillis = 0L;
    private long startTime = 0L;
    private long timeWhenPaused = 0L;
    private long restEndTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<Routine> activeTrainingDay = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> formattedTime = new MutableLiveData<>("00:00");
    private final MutableLiveData<Long> elapsedMillis = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isRestTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> restSecondsRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> restTotalSeconds = new MutableLiveData<>(0);

    private final MutableLiveData<Boolean> navigateToHome = new MutableLiveData<>(false);

    public WorkoutViewModel(TrainingRepository trainingRepository,
                            ExerciseRepository exerciseRepository,
                            HistoryRepository historyRepository,
                            SessionManager sessionManager) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.historyRepository = historyRepository;
        this.sessionManager = sessionManager;
    }

    public void checkRestoredSession() {
        if (sessionManager.isSessionActive()) {
            Routine restored = sessionManager.getSavedRoutine();
            if (restored != null) {
                this.workoutStartTimeMillis = sessionManager.getSavedStartTime();

                workoutTitle.setValue(restored.getName());
                activeTrainingDay.setValue(restored);
                isWorkoutInProgress.setValue(true);

                // Calcolo il tempo trascorso reale per riallineare il timer
                long elapsedSoFar = System.currentTimeMillis() - workoutStartTimeMillis;
                this.timeWhenPaused = elapsedSoFar;

                startWorkoutTimer();
            }
        }
    }

    public void startWorkout(Routine day, Training parentTraining) {
        if (day == null) return;
        deepResetRoutine(day);
        this.parentTraining = parentTraining;
        this.workoutStartTimeMillis = System.currentTimeMillis();
        workoutTitle.setValue(day.getName());
        activeTrainingDay.setValue(day);
        isWorkoutInProgress.setValue(true);

        navigateToHome.setValue(false); // Reset navigazione
        resetWorkoutTimer();
        startWorkoutTimer();

        sessionManager.saveSessionState(day, workoutStartTimeMillis);
    }

    public void stopWorkout(FirebaseCallback<Void> callback) {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true); // Forza uscita
        if (callback != null) callback.onSuccess(null);
    }

    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        Routine currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getWorkoutExercises() == null) return;
        List<WorkoutExercise> workoutExercises = currentDay.getWorkoutExercises();
        if (exercisePosition >= 0 && exercisePosition < workoutExercises.size()) {
            WorkoutExercise workoutExercise = workoutExercises.get(exercisePosition);
            if (workoutExercise.getSeries() != null && setPosition >= 0 && setPosition < workoutExercise.getSeries().size()) {
                Serie serie = workoutExercise.getSeries().get(setPosition);
                // RIMOSSO COMPLETED  da serie. Andrà trovato un altro modo per gestirlo
                /*
                boolean newState = !serie.isCompleted();
                serie.setCompleted(newState);
                if (newState) {
                    startRestTimer(restTimeSeconds);
                } else {
                    stopRestTimer();
                }
                */
                activeTrainingDay.setValue(currentDay);
            }
        }
    }
    public void cancelWorkout() {
        stopWorkout(null);
    }

    public void startRestTimer(int seconds) {
        timerHandler.removeCallbacks(restUpdateRunnable);
        restTotalSeconds.setValue(seconds);
        restSecondsRemaining.setValue(seconds);
        restEndTime = SystemClock.elapsedRealtime() + (seconds * 1000L);
        isRestTimerRunning.setValue(true);
        timerHandler.post(restUpdateRunnable);
    }

    public void stopRestTimer() {
        isRestTimerRunning.setValue(false);
        timerHandler.removeCallbacks(restUpdateRunnable);
        restSecondsRemaining.postValue(0);
    }

    public void skipRestTimer() { stopRestTimer(); }

    private final Runnable restUpdateRunnable = new Runnable() {
        @Override public void run() {
            if (Boolean.TRUE.equals(isRestTimerRunning.getValue())) {
                long now = SystemClock.elapsedRealtime();
                long remaining = restEndTime - now;
                if (remaining <= 0) {
                    restSecondsRemaining.postValue(0);
                    isRestTimerRunning.postValue(false);
                } else {
                    restSecondsRemaining.postValue((int) (remaining / 1000));
                    timerHandler.postDelayed(this, 250);
                }
            }
        }
    };

    private final Runnable updateRunnable = new Runnable() {
        @Override public void run() {
            if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) {
                long now = SystemClock.elapsedRealtime();
                long totalMillis = timeWhenPaused + (now - startTime);
                elapsedMillis.postValue(totalMillis);
                formattedTime.postValue(formatMillis(totalMillis));
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    public void startWorkoutTimer() {
        if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) return;
        startTime = SystemClock.elapsedRealtime() - timeWhenPaused;
        isWorkoutTimerRunning.setValue(true);
        timerHandler.post(updateRunnable);
    }

    public void pauseWorkoutTimer() {
        isWorkoutTimerRunning.postValue(false);
        timerHandler.removeCallbacks(updateRunnable);
        timeWhenPaused = SystemClock.elapsedRealtime() - startTime;
    }

                // Aggiorna i campi 'actual' dell'oggetto Serie
                // RIMOSSI actualWeight e actualReps da serie. Andrà trovato un altro modo per gestirlo
                /*
                serie.setActualWeight(actualWeight);
                serie.setActualReps(actualReps);
                 */
    private void deepResetRoutine(Routine routine) {
        if (routine == null || routine.getWorkoutExercises() == null) return;
        for (WorkoutExercise ex : routine.getWorkoutExercises()) {
            if (ex.getSeries() != null) {
                for (Serie s : ex.getSeries()) {
                    s.setCompleted(false);
                    s.setActualWeight(0);
                    s.setActualReps(0);
                }
            }
        }
    }

    private void resetWorkoutState() {
        parentTraining = null;
        workoutTitle.postValue(null);
        activeTrainingDay.postValue(null);
        isWorkoutInProgress.postValue(false);
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
    }

    private void resetWorkoutTimer() { timeWhenPaused = 0L; elapsedMillis.postValue(0L); formattedTime.postValue("00:00"); }

    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", (s % 3600) / 60, s % 60);
    }

    public void finishWorkout(Runnable onComplete) {
        finishWorkout();
        if (onComplete != null) {
            timerHandler.postDelayed(onComplete, 100);
        }
    }

    public void finishWorkout() {
        Routine currentRoutine = activeTrainingDay.getValue();
        if (currentRoutine == null) return;

        historyRepository.saveWorkout(currentRoutine, workoutStartTimeMillis, () -> {
            timerHandler.post(() -> {
                sessionManager.clearSession();
                resetWorkoutState();
                navigateToHome.setValue(true);
            });
        });
    }

    public void updateSetData(int exPos, int setPos, double weight, int reps) {
        Routine current = activeTrainingDay.getValue();
        if (current == null) return;
        Serie s = current.getWorkoutExercises().get(exPos).getSeries().get(setPos);
        s.setActualWeight(weight); s.setActualReps(reps);
        activeTrainingDay.setValue(current);
        sessionManager.saveSessionState(current, workoutStartTimeMillis);
    }

    // --- GETTERS ---
    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<Routine> getActiveTrainingDay() { return activeTrainingDay; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; }

    // CRUD METODI
    public void addSetToExercise(int pos) {
        Routine c = activeTrainingDay.getValue(); if(c == null) return;
        Serie n = new Serie(); n.setSerieNumber(c.getWorkoutExercises().get(pos).getSeries().size()+1);
        c.getWorkoutExercises().get(pos).getSeries().add(n);
        activeTrainingDay.setValue(c);
        sessionManager.saveSessionState(c, workoutStartTimeMillis);
    }
    public void deleteSetFromExercise(int exP, int setP) {
        Routine c = activeTrainingDay.getValue(); if(c == null) return;
        c.getWorkoutExercises().get(exP).getSeries().remove(setP);
        activeTrainingDay.setValue(c);
        sessionManager.saveSessionState(c, workoutStartTimeMillis);
    }
}