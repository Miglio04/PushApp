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
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.repositories.TrainingRepository;
// --- FIX 1: Import corretto (utils) ---
import com.example.pushapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    private final String TAG = "WorkoutViewModel";
    private final ExerciseRepository exerciseRepository;
    private final TrainingRepository trainingRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;

    private Training parentTraining;
    private Routine originalRoutineTemplate;
    private long workoutStartTimeMillis = 0L;
    private long startTime = 0L;
    private long timeWhenPaused = 0L;
    private long restEndTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<HistorySessionWithExercises> activeWorkoutSession = new MutableLiveData<>();
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
            HistorySessionWithExercises restoredSession = sessionManager.getSavedSession();
            if (restoredSession != null) {
                this.workoutStartTimeMillis = sessionManager.getSavedStartTime();

                workoutTitle.setValue(restoredSession.session.getName());
                activeWorkoutSession.setValue(restoredSession);
                isWorkoutInProgress.setValue(true);

                long elapsedSoFar = System.currentTimeMillis() - workoutStartTimeMillis;
                this.timeWhenPaused = elapsedSoFar;

                startWorkoutTimer();
            }
        }
    }

    public void startWorkout(Routine day, Training parentTraining) {
        if (day == null) return;
        //deepResetRoutine(day); credo non serva più
        this.parentTraining = parentTraining;
        this.originalRoutineTemplate = day;

        HistorySessionWithExercises newSession = historyRepository.createNewWorkoutSession(day);
        if (newSession == null) return; // Gestione dell'errore

        this.workoutStartTimeMillis = newSession.session.getStartTime();
        workoutTitle.setValue(newSession.session.getName());
        isWorkoutInProgress.setValue(true);
        activeWorkoutSession.setValue(newSession);

        navigateToHome.setValue(false); // Reset navigazione
        resetWorkoutTimer();
        startWorkoutTimer();
        sessionManager.saveSessionState(newSession, workoutStartTimeMillis);
    }

    public void stopWorkout(FirebaseCallback<Void> callback) {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true); // Forza uscita
        if (callback != null) callback.onSuccess(null);
    }

    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null || currentSession.exercises == null) return;
        if (exercisePosition >= 0 && exercisePosition < currentSession.exercises.size()) {
            HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
            if (exercise.historySeries != null && setPosition >= 0 && setPosition < exercise.historySeries.size()) {
                HistorySerie serie = exercise.historySeries.get(setPosition);
                boolean newState = !serie.isCompleted();
                serie.setCompleted(newState);
                if (newState) {
                    startRestTimer(restTimeSeconds);
                } else {
                    stopRestTimer();
                }
                activeWorkoutSession.postValue(currentSession);
                sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
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

    // Credo non ci serva più
    private void deepResetRoutine(Routine routine) {
        if (routine == null || routine.getWorkoutExercises() == null) return;
        for (WorkoutExercise ex : routine.getWorkoutExercises()) {
            if (ex.getSeries() != null) {
                for (Serie s : ex.getSeries()) {
                    //s.setCompleted(false);
                    //s.setActualWeight(0);
                    //s.setActualReps(0);
                }
            }
        }
    }

    private void resetWorkoutState() {
        parentTraining = null;
        workoutTitle.postValue(null);
        activeWorkoutSession.postValue(null);
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
        HistorySessionWithExercises sessionToSave = activeWorkoutSession.getValue();
        if (sessionToSave == null) return;

        long endTime = System.currentTimeMillis();
        sessionToSave.session.setEndTime(endTime);
        sessionToSave.session.setDuration(endTime - sessionToSave.session.getStartTime());

        if (sessionToSave.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries ex : sessionToSave.exercises) {
                if (ex.historySeries != null) {
                    ex.historySeries.removeIf(serie -> serie.getReps() == 0);
                }
            }
            sessionToSave.exercises.removeIf(ex -> ex.historySeries == null || ex.historySeries.isEmpty());
        }
        historyRepository.saveWorkoutSession(sessionToSave, () -> {
            // Questo codice viene eseguito quando il salvataggio è completato
            timerHandler.post(() -> {
                sessionManager.clearSession();
                resetWorkoutState();
                navigateToHome.setValue(true);
            });
        });
    }

    public void updateSetData(int exPos, int setPos, double weight, int reps) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null) return;
        HistorySerie s = currentSession.exercises.get(exPos).historySeries.get(setPos);
        s.setWeight(weight);
        s.setReps(reps);
        activeWorkoutSession.setValue(currentSession);
        sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
    }

    // --- GETTERS ---
    public Routine getOriginalRoutineTemplate() {return originalRoutineTemplate; }
    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<HistorySessionWithExercises> getActiveWorkoutSession() { return activeWorkoutSession; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; }

    // CRUD METODI
    public void addSetToExercise(int exercisePosition) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null || currentSession.exercises == null || exercisePosition >= currentSession.exercises.size()) {
            return;
        }
        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null) {
            exercise.historySeries = new ArrayList<>();
        }
        HistorySerie newSerie = new HistorySerie(
                exercise.historyWorkoutExercise.getHistoryExerciseId(),
                exercise.historySeries.size() + 1,
                0,
                0
        );
        newSerie.setCompleted(false);
        exercise.historySeries.add(newSerie);
        activeWorkoutSession.postValue(currentSession);
        sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
    }
    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null || currentSession.exercises == null || exercisePosition >= currentSession.exercises.size()) {
            return;
        }
        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null || setPosition >= exercise.historySeries.size()) {
            return;
        }
        exercise.historySeries.remove(setPosition);
        for (int i = 0; i < exercise.historySeries.size(); i++) {
            exercise.historySeries.get(i).setSetNumber(i + 1);
        }
        activeWorkoutSession.postValue(currentSession);
        sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
    }
}