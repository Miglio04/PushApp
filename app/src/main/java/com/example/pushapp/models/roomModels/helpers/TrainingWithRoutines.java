package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;

import java.util.List;

/**
 * Room Relationship class modeling a Training plan with its associated Routines.
 * Binds the Training entity to a list of Routine entities via trainingId.
 */
public class TrainingWithRoutines {
    @Embedded
    public Training training;

    @Relation(parentColumn = "trainingId", entityColumn = "trainingId")
    public List<Routine> routines;
}
