package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Routine;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.SessionManager;
import com.example.pushapp.utils.WorkoutState;

import java.util.Locale;

/**
 * ViewModel responsible for managing an active workout session.
 * Handles workout state, timers (workout duration and rest periods), and
 * interactions with the repository and session manager to save progress.
 */
public class WorkoutViewModel extends ViewModel {
    private final HistoryViewModel historyViewModel;
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

    /**
     * Constructor for WorkoutViewModel.
     *
     * @param historyViewModel   ViewModel for handling workout history and session creation.
     * @param sessionManager     Manager for persisting session state across app restarts.
     */
    public WorkoutViewModel(HistoryViewModel historyViewModel,
                            SessionManager sessionManager) {
        this.historyViewModel = historyViewModel;
        this.sessionManager = sessionManager;
    }

    /**
     * Checks if there is an active session saved in the SessionManager and restores it if found.
     */
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

    /**
     * Starts a new workout session based on the provided routine template.
     *
     * @param routine The routine to use as a template for the workout.
     */
    public void startWorkout(Routine routine) {
        if (routine == null) return;
        WorkoutState newSession = historyViewModel.createNewWorkoutSessionWithTemplate(routine);
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

    /**
     * Starts a new workout or restores an existing one if not already in progress.
     *
     * @param initialRoutine The routine to start if no workout is currently active.
     */
    public void startOrRestoreWorkout(@Nullable Routine initialRoutine) {
        Boolean inProgress = isWorkoutInProgress().getValue();
        if (inProgress != null && inProgress) {
            return;
        }
        if (initialRoutine != null) {
            startWorkout(initialRoutine);
        }
    }

    /**
     * Finishes the current workout, saves the session to history, and clears the active state.
     *
     * @param onComplete Runnable to execute after the workout is successfully saved and cleared.
     */
    public void finishWorkout(Runnable onComplete) {
        WorkoutState stateToSave = activeWorkoutState.getValue();
        if (stateToSave == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        long endTime = System.currentTimeMillis();
        stateToSave.getCurrentSession().session.setEndTime(endTime);
        stateToSave.getCurrentSession().session.setDuration(endTime - stateToSave.getCurrentSession().session.getStartTime());

        historyViewModel.saveWorkoutSession(stateToSave.getCurrentSession(), () ->
            timerHandler.post(() -> {
                sessionManager.clearSession();
                resetWorkoutState();
                navigateToHome.setValue(true);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        ));
    }

    /**
     * Stops and discards the currently active workout without saving it to history.
     *
     * @param callback Callback to be invoked after the session is cleared.
     */
    public void stopAndDiscardWorkout(FirebaseCallback<Void> callback) {
        sessionManager.clearSession();
        resetWorkoutState();
        navigateToHome.setValue(true);
        if (callback != null) callback.onSuccess(null);
    }

    /**
     * Adds a new set to the specified exercise in the current workout.
     *
     * @param exercisePosition The index of the exercise in the workout list.
     */
    public void addSetToExercise(int exercisePosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.addSetToExercise(exercisePosition);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    /**
     * Deletes a specific set from an exercise.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set to delete.
     */
    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;
        boolean success = currentState.deleteSetFromExercise(exercisePosition, setPosition);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    /**
     * Toggles the completion status of a specific set.
     * Starts the rest timer if the set is marked as completed.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @param restTimeSeconds  The rest time duration in seconds for the exercise.
     */
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

    /**
     * Updates the weight and repetition data for a specific set.
     *
     * @param exPos  The index of the exercise.
     * @param setPos The index of the set.
     * @param weight The new weight value.
     * @param reps   The new repetition count.
     */
    public void updateSetData(int exPos, int setPos, double weight, int reps) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.updateSetData(exPos, setPos, weight, reps);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    /**
     * Updates the rest time setting for a specific exercise.
     *
     * @param exercisePosition The index of the exercise.
     * @param newRestTimeIndex The new rest time index (or value representation).
     */
    public void updateExerciseRestTime(int exercisePosition, int newRestTimeIndex) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;

        boolean success = currentState.updateExerciseRestTime(exercisePosition, newRestTimeIndex);

        if (success) {
            activeWorkoutState.setValue(currentState);
            sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
        }
    }

    /**
     * Starts or resumes the main workout duration timer.
     */
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

    /**
     * Pauses the main workout duration timer.
     */
    public void pauseWorkoutTimer() {
        if (Boolean.FALSE.equals(isWorkoutTimerRunning.getValue())) return;
        pauseTimeMillis = System.currentTimeMillis();
        isWorkoutTimerRunning.postValue(false);
        timerHandler.removeCallbacks(updateRunnable);
    }

    /**
     * Starts a countdown rest timer for the specified duration.
     *
     * @param seconds The duration of the rest period in seconds.
     */
    public void startRestTimer(int seconds) {
        timerHandler.removeCallbacks(restUpdateRunnable);
        restTotalSeconds.setValue(seconds);
        restSecondsRemaining.setValue(seconds);
        restEndTime = SystemClock.elapsedRealtime() + (seconds * 1000L);
        isRestTimerRunning.setValue(true);
        timerHandler.post(restUpdateRunnable);
    }

    /**
     * Stops the current rest timer immediately.
     */
    public void stopRestTimer() {
        isRestTimerRunning.setValue(false);
        timerHandler.removeCallbacks(restUpdateRunnable);
        restSecondsRemaining.postValue(0);
    }

    /**
     * Resets the workout timer display and elapsed time to zero.
     */
    private void resetWorkoutTimer() {
        elapsedMillis.postValue(0L);
        formattedTime.postValue("00:00");
    }

    /**
     * Clears all workout state data, effectively resetting the ViewModel to an idle state.
     * Stops all timers and clears the current session.
     */
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

    /**
     * Formats milliseconds into a mm:ss string.
     *
     * @param millis The time in milliseconds to format.
     * @return A string representing the time in "mm:ss" format.
     */
    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", (s % 3600) / 60, s % 60);
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Removes any pending posts of Runnable to the Handler to prevent memory leaks.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        timerHandler.removeCallbacks(updateRunnable);
        timerHandler.removeCallbacks(restUpdateRunnable);
    }

    /**
     * Runnable that updates the rest timer countdown.
     * Decrements the remaining time every 250ms (for smooth UI updates) until it reaches zero.
     */
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

    /**
     * Runnable that updates the main workout timer.
     * Calculates the elapsed time since the workout started (accounting for pauses)
     * and updates the formatted time string every 1000ms.
     */
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

    /**
     * Getters for LiveData properties to be observed by the UI.
     */
    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<WorkoutState> getActiveWorkoutState() { return activeWorkoutState; }
}