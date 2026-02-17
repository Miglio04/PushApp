package com.example.pushapp.utils.converters;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.api.ExerciseApiModel;

/**
 * Utility class for converting Exercise API models to local Exercise domain entities.
 * Facilitates data transformation from network responses to database objects.
 */
public class ExerciseConverter {
    public static Exercise apiToExercise(ExerciseApiModel apiModel) {
        return new Exercise(
                apiModel.getName(),
                apiModel.getMuscle(),
                apiModel.getDifficulty()
        );
    }
}
