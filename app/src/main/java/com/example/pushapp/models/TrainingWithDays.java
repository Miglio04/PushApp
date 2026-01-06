package com.example.pushapp.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TrainingWithDays {
    @Embedded
    public Training training;

    @Relation(parentColumn = "id", entityColumn = "training_id")
    public List<TrainingDay> trainingDays;
}
