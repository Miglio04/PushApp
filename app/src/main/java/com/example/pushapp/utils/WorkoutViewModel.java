package com.example.pushapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.ExerciseApiModel;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.repositories.TrainingRepository;

import java.util.List;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    // --- REPOSITORIES ---
    private final ExerciseRepository exerciseRepository;
    private final TrainingRepository trainingRepository;

    // --- TRAINING PADRE ---
    private Training parentTraining;

    // --- CAMPI TIMER ---
    private long startTime = 0L;
    private long timeWhenPaused = 0L;
    private long restEndTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    // --- LIVE DATA PER LO STATO DELL'ALLENAMENTO ---
    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<TrainingDay> activeTrainingDay = new MutableLiveData<>();

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

    // --- COSTRUTTORE ---
    public WorkoutViewModel() {
        this.exerciseRepository = new ExerciseRepository();
        this.trainingRepository = new TrainingRepository();
    }

    // --- GETTERS ---
    public LiveData<String> getWorkoutTitle() {
        return workoutTitle;
    }

    public LiveData<Boolean> isWorkoutInProgress() {
        return isWorkoutInProgress;
    }

    public LiveData<TrainingDay> getActiveTrainingDay() {
        return activeTrainingDay;
    }

    public LiveData<Boolean> isWorkoutTimerRunning() {
        return isWorkoutTimerRunning;
    }

    public LiveData<String> getFormattedTime() {
        return formattedTime;
    }

    public LiveData<Boolean> isRestTimerRunning() {
        return isRestTimerRunning;
    }

    public LiveData<Integer> getRestSecondsRemaining() {
        return restSecondsRemaining;
    }

    public LiveData<Integer> getRestTotalSeconds() {
        return restTotalSeconds;
    }

    public LiveData<List<ExerciseApiModel>> getAvailableExercises() {
        return availableExercises;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // --- LOGICA DI CONTROLLO PRINCIPALE ---

    public void startWorkout(TrainingDay day, Training parentTraining) {
        if (day == null) return;
        this.parentTraining = parentTraining;
        workoutTitle.setValue(day.getName());
        activeTrainingDay.setValue(day);
        isWorkoutInProgress.setValue(true);
        resetWorkoutTimer();
        startWorkoutTimer();
    }

    public void stopWorkout(FirebaseCallback<Void> callback) {
        if (parentTraining != null) {
            // Aggiorna il training intero (che contiene il TrainingDay modificato)
            trainingRepository.updateTraining(parentTraining, new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    resetWorkoutState();
                    if (callback != null) callback.onSuccess(null);
                }

                @Override
                public void onError(Exception e) {
                    resetWorkoutState();
                    if (callback != null) callback.onError(e);
                }
            });
        } else {
            resetWorkoutState();
            if (callback != null) callback.onSuccess(null);
        }
    }

    private void resetWorkoutState() {
        parentTraining = null; // Reset anche questo
        workoutTitle.setValue(null);
        activeTrainingDay.setValue(null);
        isWorkoutInProgress.setValue(false);
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
    }

    public void toggleSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        TrainingDay currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getExercises() == null) return;
        List<Exercise> exercises = currentDay.getExercises();
        if (exercisePosition >= 0 && exercisePosition < exercises.size()) {
            Exercise exercise = exercises.get(exercisePosition);
            if (exercise.getSeries() != null && setPosition >= 0 && setPosition < exercise.getSeries().size()) {
                Serie serie = exercise.getSeries().get(setPosition);
                boolean newState = !serie.isCompleted();
                serie.setCompleted(newState);
                if (newState) {
                    startRestTimer(restTimeSeconds);
                } else {
                    stopRestTimer();
                }
                activeTrainingDay.setValue(currentDay);
            }
        }
    }


    // --- LOGICA DI RETE (API) ---

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
        startTime = SystemClock.elapsedRealtime() - timeWhenPaused; // Correzione per ripartire
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

    public void addSetToExercise(int exercisePosition) {
        TrainingDay currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getExercises() == null) return;

        List<Exercise> exercises = currentDay.getExercises();
        if (exercisePosition >= 0 && exercisePosition < exercises.size()) {
            Exercise exercise = exercises.get(exercisePosition);
            List<Serie> series = exercise.getSeries();

            if (series == null) {
                series = new java.util.ArrayList<>();
                exercise.setSeries(series);
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
            activeTrainingDay.setValue(currentDay); // Aggiorna solo la UI locale
        }
    }

    public void updateSetData(int exercisePosition, int setPosition, double actualWeight, int actualReps) {
        TrainingDay currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getExercises() == null) return;

        List<Exercise> exercises = currentDay.getExercises();
        if (exercisePosition >= 0 && exercisePosition < exercises.size()) {
            Exercise exercise = exercises.get(exercisePosition);
            if (exercise.getSeries() != null && setPosition >= 0 && setPosition < exercise.getSeries().size()) {
                Serie serie = exercise.getSeries().get(setPosition);

                // Aggiorna i campi 'actual' dell'oggetto Serie
                serie.setActualWeight(actualWeight);
                serie.setActualReps(actualReps);
            }
        }
    }

    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        TrainingDay currentDay = activeTrainingDay.getValue();
        if (currentDay == null || currentDay.getExercises() == null) return;

        List<Exercise> exercises = currentDay.getExercises();
        if (exercisePosition >= 0 && exercisePosition < exercises.size()) {
            Exercise exercise = exercises.get(exercisePosition);
            List<Serie> series = exercise.getSeries();

            if (series != null && setPosition >= 0 && setPosition < series.size()) {
                series.remove(setPosition);

                // Rinumera le serie rimanenti
                for (int i = 0; i < series.size(); i++) {
                    series.get(i).setSerieNumber(i + 1);
                }

                activeTrainingDay.setValue(currentDay);
            }
        }
    }


    // --- RUNNABLES (INVARIATI) ---
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) {
                long now = SystemClock.elapsedRealtime();
                long totalMillis = timeWhenPaused + (now - startTime);
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
        timeWhenPaused = 0L;
        elapsedMillis.setValue(0L);
        formattedTime.setValue("00:00");
    }
}
