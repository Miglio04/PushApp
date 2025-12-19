package com.example.pushapp.utils;

import com.example.pushapp.models.ExerciseSeries;

public class ExerciseUiModel {
    private final ExerciseSeries exerciseSeries;
    private boolean isExpanded;

    public ExerciseUiModel(ExerciseSeries exerciseSeries) {
        this.exerciseSeries = exerciseSeries;
        this.isExpanded = false;
    }

    public ExerciseSeries getExerciseSeries() {
        return exerciseSeries;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
