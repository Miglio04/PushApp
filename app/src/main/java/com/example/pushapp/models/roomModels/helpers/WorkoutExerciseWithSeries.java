package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;

import java.util.List;

/**
 * Room Relationship class modeling a WorkoutExercise with its associated Series.
 * Binds the WorkoutExercise entity to a list of Serie entities via workoutExerciseId.
 */
public class WorkoutExerciseWithSeries {
    @Embedded
    public WorkoutExercise workoutExercise;

    @Relation(parentColumn = "workoutExerciseId", entityColumn = "workoutExerciseId")
    public List<Serie> series;

}
