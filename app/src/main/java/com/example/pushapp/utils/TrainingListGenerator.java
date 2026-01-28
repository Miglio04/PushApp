package com.example.pushapp.utils;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainingListGenerator {

    // Contatori atomici per garantire ID interi unici (essenziale per le chiavi primarie di Room)
    private static final AtomicInteger exerciseIdCounter = new AtomicInteger(0);
    private static final AtomicInteger serieIdCounter = new AtomicInteger(0);

    /**
     * Genera una lista di Training di esempio con dati strutturati in modo relazionale.
     * Ogni oggetto ha le sue chiavi primarie e le chiavi esterne già impostate.
     * @return una ArrayList di Training pronti per essere inseriti in Room.
     */
    public static Training generateTrainingList() {

        // --- Crea il primo Training: "Push Pull Legs" ---
        Training sampleTraining = new Training("Push Pull Legs", "Split classico PPL");
        sampleTraining.setTrainingId(UUID.randomUUID().toString());

        // --- Crea le sue Routine ---
        ArrayList<Routine> pplRoutines = new ArrayList<>();

        // Routine 1: Push Day
        Routine pushDay = new Routine("Push Day", 1);
        pushDay.setTrainingId(sampleTraining.getTrainingId());

        // Crea e aggiungi gli esercizi alla routine "Push Day"
        pushDay.addWorkoutExercise(createExercise("Bench Press", 1, 4, 8, 80, pushDay.getRoutineId()));
        pushDay.addWorkoutExercise(createExercise("Overhead Press", 2, 3, 10, 40, pushDay.getRoutineId()));
        pushDay.addWorkoutExercise(createExercise("Tricep Pushdown", 3, 3, 12, 25, pushDay.getRoutineId()));
        pplRoutines.add(pushDay);

        // Routine 2: Pull Day
        Routine pullDay = new Routine("Pull Day", 2);
        pullDay.setTrainingId(sampleTraining.getTrainingId());

        // Crea e aggiungi gli esercizi alla routine "Pull Day"
        pullDay.addWorkoutExercise(createExercise("Pull Ups", 1, 4, 8, 0, pullDay.getRoutineId()));
        pullDay.addWorkoutExercise(createExercise("Barbell Row", 2, 3, 10, 60, pullDay.getRoutineId()));
        pplRoutines.add(pullDay);

        // **PASSAGGIO CHIAVE MANCANTE:** Imposta la lista di routine nel training
        sampleTraining.setRoutinesList(pplRoutines);

        return sampleTraining;
    }

    private static WorkoutExercise createExercise(String name, int order, int numSeries, int targetReps, double targetWeight, String routineId) {
        int exerciseId = (int)(Math.random() * 100000);
        WorkoutExercise workoutExercise = new WorkoutExercise(exerciseId, name, order);
        workoutExercise.setRoutineId(routineId);

        // Crea le sue serie
        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < numSeries; i++) {
            int serieId = (int)(Math.random() * 100000);
            Serie serie = new Serie(i + 1, targetReps, targetWeight);
            serie.setSerieId(serieId);
            serie.setWorkoutExerciseId(exerciseId);
            series.add(serie);
        }
        // **PASSAGGIO CHIAVE MANCANTE:** Imposta la lista di serie nell'esercizio
        workoutExercise.setSeries(series);

        return workoutExercise;
    }
}
