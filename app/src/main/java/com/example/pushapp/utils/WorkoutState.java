package com.example.pushapp.utils;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import java.util.ArrayList;

public class WorkoutState {
    private HistorySessionWithExercises currentSession;
    private Routine originalTemplate;
    public WorkoutState(HistorySessionWithExercises currentSession, Routine originalTemplate) {
        this.currentSession = currentSession;
        this.originalTemplate = originalTemplate;
    }

    public Routine getOriginalTemplate() {
        return originalTemplate;
    }

    public HistorySessionWithExercises getCurrentSession() {
        return currentSession;
    }

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
            newTemplateSerie.setSerieNumber(templateExercise.getSeries().size() + 1);
            if (!templateExercise.getSeries().isEmpty()) {
                Serie lastTemplateSerie = templateExercise.getSeries().get(templateExercise.getSeries().size() - 1);
                newTemplateSerie.setTargetWeight(lastTemplateSerie.getTargetWeight());
                newTemplateSerie.setTargetReps(lastTemplateSerie.getTargetReps());
            }
            templateExercise.getSeries().add(newTemplateSerie);
        }

        return true;
    }

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

    public boolean toggleSetCompleted(int exercisePosition, int setPosition) {
        HistorySerie serie = findSerie(exercisePosition, setPosition);
        if (serie == null) {
            return false;
        }

        boolean newState = !serie.getIsCompleted();
        serie.setIsCompleted(newState);
        return newState;
    }

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
