package com.example.pushapp.utils.converters;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.api.ExerciseApiModel;

public class ExerciseConverter {
    public static Exercise apiToExercise(ExerciseApiModel apiModel) {
        return new Exercise(
                apiModel.getName(),
                apiModel.getMuscle(),
                apiModel.getDifficulty()
        );
    }
}
