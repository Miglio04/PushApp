package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;

import java.util.List;

// helper class used by Room queries.
public class WorkoutExerciseWithSeries {
    @Embedded
    public WorkoutExercise workoutExercise;

    @Relation(parentColumn = "workoutExerciseId", entityColumn = "workoutExerciseId")
    public List<Serie> series;

}
