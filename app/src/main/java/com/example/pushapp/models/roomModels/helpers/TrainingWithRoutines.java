package com.example.pushapp.models.roomModels.helpers;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;

import java.util.List;

public class TrainingWithRoutines {
    @Embedded
    public Training training;

    @Relation(parentColumn = "trainingId", entityColumn = "trainingId")
    public List<Routine> routines;
}
