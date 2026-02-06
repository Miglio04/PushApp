package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;

import java.util.List;

public class HistorySessionWithExercises {

    @Embedded
    public HistorySession session;

    @Relation(
            entity = HistoryWorkoutExercise.class,
            parentColumn = "sessionId",            // ID nella tabella sessioni
            entityColumn = "sessionId"             // ID nella tabella esercizi
    )
    public List<HistoryWorkoutExerciseWithSeries> exercises;
}