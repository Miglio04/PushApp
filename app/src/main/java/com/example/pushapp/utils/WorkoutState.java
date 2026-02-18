package com.example.pushapp.utils;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import java.util.ArrayList;

/**
 * Manages the state of an active workout session.
 * Holds the current session data (exercises, sets) and the original routine template used.
 * Provides methods to modify the session state (add/remove sets, update data, toggle completion).
 */
public class WorkoutState {
    private final HistorySessionWithExercises currentSession;
    private final Routine originalTemplate;

    /**
     * Constructs a new WorkoutState with the given session and template.
     *
     * @param currentSession   The active history session containing exercises and sets.
     * @param originalTemplate The routine template the session is based on.
     */
    public WorkoutState(HistorySessionWithExercises currentSession, Routine originalTemplate) {
        this.currentSession = currentSession;
        this.originalTemplate = originalTemplate;
    }

    /**
     * Returns the original routine template used for this workout.
     */
    public Routine getOriginalTemplate() {
        return originalTemplate;
    }

    /**
     * Returns the current active history session with its exercises.
     */
    public HistorySessionWithExercises getCurrentSession() {
        return currentSession;
    }

    /**
     * Adds a new set to the specified exercise.
     * Also attempts to add a corresponding template set to the original routine structure if available.
     *
     * @param exercisePosition The index of the exercise to add a set to.
     * @return true if the set was successfully added, false otherwise.
     */
    public boolean addSetToExercise(int exercisePosition) {
        if (currentSession == null || currentSession.exercises == null || exercisePosition < 0 || exercisePosition >= currentSession.exercises.size()) {
            return false;
        }

        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null) {
            exercise.historySeries = new ArrayList<>();
        }

        HistorySerie newHistorySerie = new HistorySerie(
                exercise.historyWorkoutExercise.getHistoryExerciseId(),
                exercise.historySeries.size() + 1,
                0,
                0
        );
        newHistorySerie.setIsCompleted(false);
        exercise.historySeries.add(newHistorySerie);

        if (originalTemplate != null && originalTemplate.getWorkoutExercises() != null && exercisePosition < originalTemplate.getWorkoutExercises().size()) {
            WorkoutExercise templateExercise = originalTemplate.getWorkoutExercises().get(exercisePosition);
            if (templateExercise.getSeries() == null) {
                templateExercise.setSeries(new ArrayList<>());
            }
            Serie newTemplateSerie = new Serie();
            if (!templateExercise.getSeries().isEmpty()) {
                Serie lastTemplateSerie = templateExercise.getSeries().get(templateExercise.getSeries().size() - 1);
                newTemplateSerie.setTargetWeight(lastTemplateSerie.getTargetWeight());
                newTemplateSerie.setTargetReps(lastTemplateSerie.getTargetReps());
            }
            templateExercise.getSeries().add(newTemplateSerie);
        }

        return true;
    }

    /**
     * Deletes a specific set from an exercise and renumbers the remaining sets.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set to delete.
     * @return true if the set was successfully deleted, false otherwise.
     */
    public boolean deleteSetFromExercise(int exercisePosition, int setPosition) {
        if (currentSession == null || currentSession.exercises == null || exercisePosition < 0 || exercisePosition >= currentSession.exercises.size()) {
            return false;
        }

        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null || setPosition < 0 || setPosition >= exercise.historySeries.size()) {
            return false;
        }
        exercise.historySeries.remove(setPosition);

        for (int i = 0; i < exercise.historySeries.size(); i++) {
            exercise.historySeries.get(i).setSetNumber(i + 1);
        }

        return true;
    }

    /**
     * Updates the weight and repetition data for a specific set.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @param weight           The new weight value.
     * @param reps             The new repetition count.
     * @return true if the update was successful, false if indices were invalid.
     */
    public boolean updateSetData(int exercisePosition, int setPosition, double weight, int reps) {
        if (currentSession == null || currentSession.exercises == null || exercisePosition < 0 || exercisePosition >= currentSession.exercises.size()) {
            return false;
        }

        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null || setPosition < 0 || setPosition >= exercise.historySeries.size()) {
            return false;
        }

        HistorySerie serie = exercise.historySeries.get(setPosition);
        serie.setWeight(weight);
        serie.setReps(reps);

        return true;
    }

    /**
     * Toggles the completion status of a specific set.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @return The new completion state (true for completed, false for not completed).
     */
    public boolean toggleSetCompleted(int exercisePosition, int setPosition) {
        HistorySerie serie = findSerie(exercisePosition, setPosition);
        if (serie == null) {
            return false;
        }

        boolean newState = !serie.getIsCompleted();
        serie.setIsCompleted(newState);
        return newState;
    }

    /**
     * Updates the rest time index for a specific exercise.
     *
     * @param exercisePosition The index of the exercise.
     * @param newRestTimeIndex The new rest time index value.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateExerciseRestTime(int exercisePosition, int newRestTimeIndex) {
        if (currentSession == null || currentSession.exercises == null || exercisePosition < 0 || exercisePosition >= currentSession.exercises.size()) {
            return false;
        }

        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historyWorkoutExercise != null) {
            exercise.historyWorkoutExercise.setCurrentRestTimeIndex(newRestTimeIndex);
            return true;
        }
        return false;
    }

    /**
     * Finds and returns a specific HistorySerie based on exercise and set position.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @return The HistorySerie at the specified position, or null if not found.
     */
    private HistorySerie findSerie(int exercisePosition, int setPosition) {
        if (currentSession == null || currentSession.exercises == null || exercisePosition < 0 || exercisePosition >= currentSession.exercises.size()) {
            return null;
        }
        HistoryWorkoutExerciseWithSeries exercise = currentSession.exercises.get(exercisePosition);
        if (exercise.historySeries == null || setPosition < 0 || setPosition >= exercise.historySeries.size()) {
            return null;
        }
        return exercise.historySeries.get(setPosition);
    }
}
