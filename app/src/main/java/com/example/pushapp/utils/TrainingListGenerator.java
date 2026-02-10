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
        Training sampleTraining = new Training();
        sampleTraining.setName("Push Pull Legs");
        sampleTraining.setDescription("Split classico PPL");
        ArrayList<Routine> pplRoutines = new ArrayList<>();

        // Routine 1: Push Day
        Routine pushDay = new Routine("Push Day", 1);

        // Crea e aggiungi gli esercizi alla routine "Push Day"
        pushDay.addWorkoutExercise(createExercise( 1, 4, 8, 80));
        pushDay.addWorkoutExercise(createExercise( 2, 3, 10, 40));
        pushDay.addWorkoutExercise(createExercise( 3, 3, 12, 25));
        pplRoutines.add(pushDay);

        // Routine 2: Pull Day
        Routine pullDay = new Routine("Pull Day", 2);

        // Crea e aggiungi gli esercizi alla routine "Pull Day"
        pullDay.addWorkoutExercise(createExercise( 1, 4, 8, 0));
        pullDay.addWorkoutExercise(createExercise( 2, 3, 10, 60));
        pplRoutines.add(pullDay);

        // **PASSAGGIO CHIAVE MANCANTE:** Imposta la lista di routine nel training
        sampleTraining.setRoutinesList(pplRoutines);

        return sampleTraining;
    }

    private static WorkoutExercise createExercise(int order, int numSeries, int targetReps, double targetWeight) {
        WorkoutExercise workoutExercise = new WorkoutExercise("PlaceHolderStringaAPIExerciseID", order);

        // Crea le sue serie
        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < numSeries; i++) {
            Serie serie = new Serie(i + 1, targetReps, targetWeight);
            series.add(serie);
        }
        // **PASSAGGIO CHIAVE MANCANTE:** Imposta la lista di serie nell'esercizio
        workoutExercise.setSeries(series);

        return workoutExercise;
    }
}