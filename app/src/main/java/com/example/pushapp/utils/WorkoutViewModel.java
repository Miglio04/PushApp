//Probabilmente da cambiare package. Magari una cartella viewmodel o simile
package com.example.pushapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutViewModel extends ViewModel {

    // Utility
    private long startTime = 0L;
    private long timeWhenPaused = 0L;

    // Workout State
    private final MutableLiveData<String> workoutTitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWorkoutInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<List<WorkoutCard>> workoutCards = new MutableLiveData<>(new ArrayList<>());

    // Workout Timer State
    private final MutableLiveData<Boolean> isWorkoutTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> formattedTime = new MutableLiveData<>("00:00");
    private final MutableLiveData<Long> elapsedMillis = new MutableLiveData<>(0L);

    // Rest Timer State
    private final MutableLiveData<Boolean> isRestTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> restSecondsRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> restTotalSeconds = new MutableLiveData<>(0);
    private long restEndTime = 0L;

    // Handler for both workout and rest timers
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    // Runnable for workout timer updates
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

    // Runnable for rest timer updates
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

    // LiveData Getters for workout
    public LiveData<String> getWorkoutTitle() { return workoutTitle; }
    public LiveData<Boolean> isWorkoutInProgress() { return isWorkoutInProgress; }
    public LiveData<Boolean> isWorkoutTimerRunning() { return isWorkoutTimerRunning; }
    public LiveData<String> getFormattedTime() { return formattedTime; }
    public LiveData<List<WorkoutCard>> getWorkoutCards() { return workoutCards; }

    // Getters for rest timer
    public LiveData<Boolean> isRestTimerRunning() { return isRestTimerRunning; }
    public LiveData<Integer> getRestSecondsRemaining() { return restSecondsRemaining; }
    public LiveData<Integer> getRestTotalSeconds() { return restTotalSeconds; }

    // Public Control Methods

    // Workout control methods
    public void startWorkout(String title) {
        workoutTitle.setValue(title);
        isWorkoutInProgress.setValue(true);
        resetWorkoutTimer();
        startWorkoutTimer();
        // Inizializza le card solo se non esistono già
        if (workoutCards.getValue() == null || workoutCards.getValue().isEmpty()) {
            workoutCards.setValue(generateInitialCards());
        }
    }
    
    public void stopWorkout() {
        workoutTitle.setValue(null);
        isWorkoutInProgress.setValue(false);
        pauseWorkoutTimer();
        resetWorkoutTimer();
        stopRestTimer();
        // Reset cards
        workoutCards.setValue(new ArrayList<>());
    }

    // Workout timer methods
    public void startWorkoutTimer() {
        if (Boolean.TRUE.equals(isWorkoutTimerRunning.getValue())) return;
        startTime = SystemClock.elapsedRealtime();
        isWorkoutTimerRunning.setValue(true);
        timerHandler.post(updateRunnable);
    }

    public void pauseWorkoutTimer() {
        if (Boolean.FALSE.equals(isWorkoutTimerRunning.getValue())) return;
        timeWhenPaused = elapsedMillis.getValue() != null ? elapsedMillis.getValue() : 0L;
        isWorkoutTimerRunning.setValue(false);
        timerHandler.removeCallbacks(updateRunnable);
    }

    // Rest timer methods
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

    public void updateCard(int position, WorkoutCard card) {
        List<WorkoutCard> current = workoutCards.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.set(position, card);
            workoutCards.setValue(current);
        }
    }

    public void updateNoteForCard(int cardPosition, String newNote) {
        List<WorkoutCard> currentCards = workoutCards.getValue();
        if (currentCards != null && cardPosition >= 0 && cardPosition < currentCards.size()) {
            currentCards.get(cardPosition).setNote(newNote);
        }
    }

    public void updateRestTimeForCard(int cardPosition, int newRestTime) {
        List<WorkoutCard> currentCards = workoutCards.getValue();
        if (currentCards != null && cardPosition >= 0 && cardPosition < currentCards.size()) {
            currentCards.get(cardPosition).setRestTimeSeconds(newRestTime);
        }
    }

    public void setSetCompletedState(int cardPosition, int setPosition) {
        List<WorkoutCard> currentCards = workoutCards.getValue();
        if (currentCards == null) return;

        if (cardPosition >= 0 && cardPosition < currentCards.size()) {
            WorkoutCard card = currentCards.get(cardPosition);
            List<WorkoutSet> sets = card.getSets();

            if (sets != null && setPosition >= 0 && setPosition < sets.size()) {
                WorkoutSet set = sets.get(setPosition);
                boolean newState = !set.isCompleted();
                set.setCompleted(newState);
                if (newState) {
                    startRestTimer(card.getRestTimeSeconds());
                } else {
                    stopRestTimer();
                }

                workoutCards.setValue(currentCards);
            }
        }
    }

    // Metodo non testato
    public void setWorkoutCards(List<WorkoutCard> cards) {
        workoutCards.setValue(cards);
    }

    // Metodo non testato
    public void addSetToCard(int cardPosition, WorkoutSet set) {
        List<WorkoutCard> current = workoutCards.getValue();
        if (current != null && cardPosition >= 0 && cardPosition < current.size()) {
            current.get(cardPosition).addSet(set);
            workoutCards.setValue(current);
        }
    }

    private List<WorkoutCard> generateInitialCards() {
        List<WorkoutCard> cards = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            WorkoutCard c = new WorkoutCard("Exercise " + i, "Exercise description " + i, R.drawable.ic_launcher_foreground);
            c.addSet(new WorkoutSet(420f, 69));
            cards.add(c);
        }
        return cards;
    }

    // Utility
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

    @Override
    protected void onCleared() {
        super.onCleared();
        timerHandler.removeCallbacks(updateRunnable);
        timerHandler.removeCallbacks(restUpdateRunnable);
    }
}

