package com.example.pushapp.repositories;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;

/**
 * Sample data source used for debugging only.
 *
 * <p>Provides a hard-coded list of {@link Exercise} instances and notifies the result
 * via an {@code ExerciseCallback} set with {@link #setCallback(ExerciseCallback)}.
 * This data source is synchronous and designed exclusively for development/debug environments.</p>
 *
 * @see Exercise
 * @see ExerciseCallback
 */
public class ExerciseSampleDataSource {

    /**
     * Callback used to return the results.
     *
     * <p>May be {@code null} if no listener has been set.</p>
     */
    private ExerciseCallback callback = null;

    /**
     * Empty constructor.
     */
    public ExerciseSampleDataSource() {}

    /**
     * Sets the callback to be notified when sample data is ready.
     *
     * @param callback an {@code ExerciseCallback} instance or {@code null} to remove the listener
     */
    public void setCallback(ExerciseCallback callback){
        this.callback = callback;
    }

    /**
     * Retrieves sample exercises and, if set, notifies the {@code callback}
     * by invoking {@code onSuccessFromRemote} with the exercise list.
     *
     * <p>The list is generated locally and does not perform any network call.</p>
     */
    public void getSampleExercises() {
        ArrayList<Exercise> sampleExercises = new ArrayList<>();
        sampleExercises.add(new Exercise("Exercise debug 1", "chest", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 2", "chest", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 3", "back", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 4", "back", "intermediate"));
        sampleExercises.add(new Exercise("Exercise debug 5", "biceps", "intermediate"));
        sampleExercises.add(new Exercise("Exercise debug 6", "biceps", "expert"));
        sampleExercises.add(new Exercise("Exercise debug 7", "triceps", "expert"));

        if(callback != null) {
            callback.onSuccessFromRemote(sampleExercises);
        }
    }
}
