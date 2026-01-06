package com.example.pushapp.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TrainingDayWithExercises {
    @Embedded
    public TrainingDay trainingDay;

    @Relation(parentColumn = "id", entityColumn = "training_day_id")
    public List<Exercise> exercises;
}
