package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Routine;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository;
// --- FIX 1: Import corretto (utils) ---
import com.example.pushapp.utils.SessionManager;
import com.example.pushapp.utils.WorkoutState;

import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    private final String TAG = "WorkoutViewModel";
    private final ExerciseRepository exerciseRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;
    private long workoutStartTimeMillis = 0L;
    private long pauseTimeMillis = 0L;
    private long totalPausedTimeMillis = 0L;
    private long restEndTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<WorkoutState> activeWorkoutState = new MutableLiveData<>();
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
            WorkoutState restoredSession = sessionManager.getSavedSession();
            if (restoredSession != null) {
                this.workoutStartTimeMillis = sessionManager.getSavedStartTime();
                activeWorkoutState.setValue(restoredSession);
                workoutTitle.setValue(restoredSession.getCurrentSession().session.getName());
                isWorkoutInProgress.setValue(true);
                startWorkoutTimer();
            }
        }
    }

    public void startWorkout(Routine day) {
        if (day == null) return;
        WorkoutState newSession = historyRepository.createNewWorkoutSessionWithTemplate(day);
        if (newSession == null) return;

        this.workoutStartTimeMillis = newSession.getCurrentSession().session.getStartTime();
        this.totalPausedTimeMillis = 0L;
        this.pauseTimeMillis = 0L;
        activeWorkoutState.setValue(newSession);
        workoutTitle.setValue(newSession.getCurrentSession().session.getName());
        isWorkoutInProgress.setValue(true);
        navigateToHome.setValue(false);
        resetWorkoutTimer();
        startWorkoutTimer();
        sessionManager.saveSessionState(newSession, workoutStartTimeMillis);
    }

    public void startOrRestoreWorkout(@Nullable Routine dayToStart) {
        Boolean inProgress = isWorkoutInProgress().getValue();
        if (inProgress != null && inProgress) {
            return;
        }
        if (dayToStart != null) {
            startWorkout(dayToStart);
        }
    }

    public void finishWorkout(Runnable onComplete) {
        WorkoutState stateToSave = activeWorkoutState.getValue();
        if (stateToSave == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        long endTime = System.currentTimeMillis();
        stateToSave.getCurrentSession().session.setEndTime(endTime);
        stateToSave.getCurrentSession().session.setDuration(endTime - stateToSave.getCurrentSession().session.getStartTime());

        historyRepository.saveWorkoutSession(stateToSave.getCurrentSession(), () -> {
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

    public void stopAndDiscardWorkout(FirebaseCallback<Void> callback) {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true);
        if (callback != null) callback.onSuccess(null);
    }

    public void addSetToExercise(int exercisePosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.addSetToExercise(exercisePosition);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;
        boolean success = currentState.deleteSetFromExercise(exercisePosition, setPosition);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;
        boolean isNowCompleted = currentState.toggleSetCompleted(exercisePosition, setPosition);
        if (isNowCompleted) {
            startRestTimer(restTimeSeconds);
        } else {
            stopRestTimer();
        }
        activeWorkoutState.setValue(currentState);
        sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
    }

    public void updateSetData(int exPos, int setPos, double weight, int reps) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.updateSetData(exPos, setPos, weight, reps);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    public void updateExerciseRestTime(int exercisePosition, int newRestTimeIndex) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.updateExerciseRestTime(exercisePosition, newRestTimeIndex);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    public void startWorkoutTimer() {
        if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) return;
        if (pauseTimeMillis > 0) {
            long pausedDuration = System.currentTimeMillis() - pauseTimeMillis;
            totalPausedTimeMillis += pausedDuration;
        }
        pauseTimeMillis = 0L;
        isWorkoutTimerRunning.setValue(true);
        timerHandler.post(updateRunnable);
    }

    public void pauseWorkoutTimer() {
        if (Boolean.FALSE.equals(isWorkoutTimerRunning.getValue())) return;
        pauseTimeMillis = System.currentTimeMillis();
        isWorkoutTimerRunning.postValue(false);
        timerHandler.removeCallbacks(updateRunnable);
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

    private void resetWorkoutTimer() {
        elapsedMillis.postValue(0L);
        formattedTime.postValue("00:00");
    }


    private void resetWorkoutState() {
        workoutTitle.postValue(null);
        activeWorkoutState.postValue(null);
        isWorkoutInProgress.postValue(false);
        workoutStartTimeMillis = 0L;
        totalPausedTimeMillis = 0L;
        pauseTimeMillis = 0L;
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
    }

    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", (s % 3600) / 60, s % 60);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        timerHandler.removeCallbacks(updateRunnable);
        timerHandler.removeCallbacks(restUpdateRunnable);
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
                    long totalMillis = System.currentTimeMillis() - workoutStartTimeMillis - totalPausedTimeMillis;
                    elapsedMillis.postValue(totalMillis);
                    formattedTime.postValue(formatMillis(totalMillis));
                }
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; }
    public LiveData<WorkoutState> getActiveWorkoutState() { return activeWorkoutState; }
}