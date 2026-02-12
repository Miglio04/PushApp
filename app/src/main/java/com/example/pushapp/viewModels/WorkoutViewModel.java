package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository;
// --- FIX 1: Import corretto (utils) ---
import com.example.pushapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    private final String TAG = "WorkoutViewModel";
    private final ExerciseRepository exerciseRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;

    private Routine originalRoutineTemplate;
    private long workoutStartTimeMillis = 0L;
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

    public WorkoutViewModel(ExerciseRepository exerciseRepository,
                            HistoryRepository historyRepository,
                            SessionManager sessionManager) {
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

                startWorkoutTimer();
            }
        }
    }

    public void startWorkout(Routine day) {
        if (day == null) return;
        this.originalRoutineTemplate = day;

        HistorySessionWithExercises newSession = historyRepository.createNewWorkoutSession(day);
        if (newSession == null) return;

        this.workoutStartTimeMillis = newSession.session.getStartTime();
        workoutTitle.setValue(newSession.session.getName());
        isWorkoutInProgress.setValue(true);
        activeWorkoutSession.setValue(newSession);

        navigateToHome.setValue(false);
        resetWorkoutTimer();
        startWorkoutTimer();
        sessionManager.saveSessionState(newSession, workoutStartTimeMillis);
    }

    public void stopAndDiscardWorkout(FirebaseCallback<Void> callback) {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true);
        if (callback != null) callback.onSuccess(null);
    }

    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null) return;
        boolean isNowCompleted = updateSetCompletedStatus(exercisePosition, setPosition);
        if (isNowCompleted) {
            startRestTimer(restTimeSeconds);
        } else {
            stopRestTimer();
        }
        sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
    }

    private boolean updateSetCompletedStatus(int exercisePosition, int setPosition) {
        HistorySessionWithExercises currentSession = activeWorkoutSession.getValue();
        if (currentSession == null || currentSession.exercises == null) return false;

        if (exercisePosition >= 0 && exercisePosition < currentSession.exercises.size()) {
            HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);

            if (exercise.historySeries != null && setPosition >= 0 && setPosition < exercise.historySeries.size()) {
                HistorySerie serie = exercise.historySeries.get(setPosition);
                boolean newState = !serie.getIsCompleted();
                serie.setIsCompleted(newState);
                activeWorkoutSession.setValue(currentSession);
                return newState;
            }
        }
        return false;
    }

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
        newSerie.setIsCompleted(false);
        exercise.historySeries.add(newSerie);
        activeWorkoutSession.setValue(currentSession);
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
        activeWorkoutSession.setValue(currentSession);
        sessionManager.saveSessionState(currentSession, workoutStartTimeMillis);
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
                if (workoutStartTimeMillis > 0) {
                    long totalMillis = System.currentTimeMillis() - workoutStartTimeMillis;
                    elapsedMillis.postValue(totalMillis);
                    formattedTime.postValue(formatMillis(totalMillis));
                }
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    public void startWorkoutTimer() {
        if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) return;
        isWorkoutTimerRunning.setValue(true);
        timerHandler.post(updateRunnable);
    }

    public void pauseWorkoutTimer() {
        isWorkoutTimerRunning.postValue(false);
        timerHandler.removeCallbacks(updateRunnable);
    }

    private void resetWorkoutTimer() {
        elapsedMillis.postValue(0L);
        formattedTime.postValue("00:00");
    }


    private void resetWorkoutState() {
        workoutTitle.postValue(null);
        activeWorkoutSession.postValue(null);
        isWorkoutInProgress.postValue(false);
        workoutStartTimeMillis = 0L;
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
    }

    public void finishWorkout(Runnable onComplete) {
        HistorySessionWithExercises sessionToSave = activeWorkoutSession.getValue();
        if (sessionToSave == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        long endTime = System.currentTimeMillis();
        sessionToSave.session.setEndTime(endTime);
        sessionToSave.session.setDuration(endTime - sessionToSave.session.getStartTime());

        historyRepository.saveWorkoutSession(sessionToSave, () -> {
            timerHandler.post(() -> {
                sessionManager.clearSession();
                resetWorkoutState();
                navigateToHome.setValue(true);
                if (onComplete != null) {
                    onComplete.run();
                }
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

    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", (s % 3600) / 60, s % 60);
    }

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
}