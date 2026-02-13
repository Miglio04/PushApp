package com.example.pushapp.viewModels;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.HistoryRepository;
// --- FIX 1: Import corretto (utils) ---
import com.example.pushapp.utils.SessionManager;
import com.example.pushapp.utils.WorkoutState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    private final String TAG = "WorkoutViewModel";
    private final ExerciseRepository exerciseRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;

    //private Routine originalRoutineTemplate;
    private long workoutStartTimeMillis = 0L;
    private long restEndTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<WorkoutState> activeWorkoutState = new MutableLiveData<>();
    //private final MutableLiveData<HistorySessionWithExercises> activeWorkoutSession = new MutableLiveData<>();
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
        activeWorkoutState.setValue(newSession);
        workoutTitle.setValue(newSession.getCurrentSession().session.getName());
        isWorkoutInProgress.setValue(true);
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
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;
        boolean isNowCompleted = updateSetCompletedStatus(exercisePosition, setPosition);
        if (isNowCompleted) {
            startRestTimer(restTimeSeconds);
        } else {
            stopRestTimer();
        }
        sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
    }

    private boolean updateSetCompletedStatus(int exercisePosition, int setPosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null || currentState.getCurrentSession().exercises == null) return false;

        if (exercisePosition >= 0 && exercisePosition < currentState.getCurrentSession().exercises.size()) {
            HistoryWorkoutExerciseWithSeries exercise = currentState.getCurrentSession().exercises.get(exercisePosition);

            if (exercise.historySeries != null && setPosition >= 0 && setPosition < exercise.historySeries.size()) {
                HistorySerie serie = exercise.historySeries.get(setPosition);
                boolean newState = !serie.getIsCompleted();
                serie.setIsCompleted(newState);
                activeWorkoutState.setValue(currentState);
                return newState;
            }
        }
        return false;
    }

    public void addSetToExercise(int exercisePosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null || currentState.getCurrentSession().exercises == null || exercisePosition >= currentState.getCurrentSession().exercises.size()) {
            return;
        }
        HistoryWorkoutExerciseWithSeries exercise = currentState.getCurrentSession().exercises.get(exercisePosition);
        if (exercise.historySeries == null) {
            exercise.historySeries = new ArrayList<>();
        }

        Routine template = currentState.getOriginalTemplate();
        Serie newTemplateSerie = null;
        if (template != null && template.getWorkoutExercises() != null && exercisePosition < template.getWorkoutExercises().size()) {
            WorkoutExercise templateExercise = template.getWorkoutExercises().get(exercisePosition);
            List<Serie> templateSeries = templateExercise.getSeries();

            if (templateSeries != null && !templateSeries.isEmpty()) {
                Serie lastTemplateSerie = templateSeries.get(templateSeries.size() - 1);
                newTemplateSerie = new Serie();
                newTemplateSerie.setTargetWeight(lastTemplateSerie.getTargetWeight());
                newTemplateSerie.setTargetReps(lastTemplateSerie.getTargetReps());
                newTemplateSerie.setSerieNumber(templateSeries.size() + 1);
                templateSeries.add(newTemplateSerie);
            }
        }


        HistorySerie newHistorySerie = new HistorySerie(
                exercise.historyWorkoutExercise.getHistoryExerciseId(),
                exercise.historySeries.size() + 1,
                0,
                0
        );
        newHistorySerie.setIsCompleted(false);
        exercise.historySeries.add(newHistorySerie);
        activeWorkoutState.setValue(currentState);
        sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
    }
    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null || currentState.getCurrentSession().exercises == null || exercisePosition >= currentState.getCurrentSession().exercises.size()) {
            return;
        }
        HistoryWorkoutExerciseWithSeries exercise = currentState.getCurrentSession().exercises.get(exercisePosition);
        if (exercise.historySeries == null || setPosition >= exercise.historySeries.size()) {
            return;
        }
        exercise.historySeries.remove(setPosition);
        for (int i = 0; i < exercise.historySeries.size(); i++) {
            exercise.historySeries.get(i).setSetNumber(i + 1);
        }
        activeWorkoutState.setValue(currentState);
        sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
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
        activeWorkoutState.postValue(null);
        isWorkoutInProgress.postValue(false);
        workoutStartTimeMillis = 0L;
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
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

    public void updateSetData(int exPos, int setPos, double weight, int reps) {
        WorkoutState currentState = activeWorkoutState.getValue();
        if (currentState == null) return;
        HistorySerie s = currentState.getCurrentSession().exercises.get(exPos).historySeries.get(setPos);
        s.setWeight(weight);
        s.setReps(reps);
        activeWorkoutState.setValue(currentState);
        sessionManager.saveSessionState(currentState, workoutStartTimeMillis);
    }

    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", (s % 3600) / 60, s % 60);
    }

    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; }
    public MutableLiveData<WorkoutState> getActiveWorkoutState() { return activeWorkoutState; }
}