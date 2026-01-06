package com.example.pushapp.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

// helper class used by Room queries.
public class ExerciseWithSeries {
    @Embedded
    public Exercise exercise;

    @Relation(parentColumn = "id", entityColumn = "exerciseId")
    public List<Serie> series;

}
