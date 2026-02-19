package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Routine;

import java.util.List;

/**
 * Room Relationship class modeling a Routine with its associated WorkoutExercises.
 * Binds the Routine entity to a list of WorkoutExercise entities via routineId.
 */
public class RoutineWithWorkoutExercises {
    @Embedded
    public Routine routine;

    @Relation(parentColumn = "routineId", entityColumn = "routineId")
    public List<WorkoutExercise> workoutExercises;
}
