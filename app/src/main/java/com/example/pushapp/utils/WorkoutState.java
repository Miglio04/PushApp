package com.example.pushapp.utils;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
public class WorkoutState {
    private HistorySessionWithExercises currentSession;
    private Routine originalTemplate;
    public WorkoutState(HistorySessionWithExercises currentSession, Routine originalTemplate) {
        this.currentSession = currentSession;
        this.originalTemplate = originalTemplate;
    }

    public Routine getOriginalTemplate() {
        return originalTemplate;
    }

    public HistorySessionWithExercises getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(HistorySessionWithExercises currentSession) {
        this.currentSession = currentSession;
    }

    public void setOriginalTemplate(Routine originalTemplate) {
        this.originalTemplate = originalTemplate;
    }
}
