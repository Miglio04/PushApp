package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistoryWorkoutExercise;

import java.util.List;

/**
 * Room Relationship class modeling a HistoryWorkoutExercise with its associated series.
 * Uses @Relation to bind the exercise (parent) to its list of HistorySerie (children).
 */
public class HistoryWorkoutExerciseWithSeries {

    @Embedded
    public HistoryWorkoutExercise historyWorkoutExercise;

    @Relation(
            parentColumn = "historyExerciseId", // ID nella tabella esercizi (padre)
            entityColumn = "historyExerciseId"  // ID nella tabella serie (figlio)
    )
    public List<HistorySerie> historySeries;
}