package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;

import java.util.List;

/**
 * Room Relationship class modeling a HistorySession with its associated exercises.
 * Uses @Relation to bind the session (parent) to its list of HistoryWorkoutExerciseWithSeries (children).
 */
public class HistorySessionWithExercises {

    @Embedded
    public HistorySession session;

    @Relation(
            entity = HistoryWorkoutExercise.class,
            parentColumn = "historySessionId",
            entityColumn = "historySessionId"
    )
    public List<HistoryWorkoutExerciseWithSeries> exercises;
}