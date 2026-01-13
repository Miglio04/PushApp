package com.example.pushapp.utils;

import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine;

import java.util.ArrayList;
import java.util.List;

public class TrainingListGenerator {

    public static ArrayList<Training> generateTrainingList() {
        ArrayList<Training> trainings = new ArrayList<>();
        Training ppl = new Training("Push Pull Legs", "Split classico PPL");
        ppl.setRoutinesList(generatePPLDays());
        trainings.add(ppl);
        return trainings;
    }

    private static ArrayList<Routine> generatePPLDays() {
        ArrayList<Routine> days = new ArrayList<>();
        Routine pushDay = new Routine("Push Day", 1);
        pushDay.addWorkoutExercise(createExercise("Bench Press", "Petto", 1, 4, 8, 80));
        pushDay.addWorkoutExercise(createExercise("Overhead Press", "Spalle", 2, 3, 10, 40));
        days.add(pushDay);
        // ... aggiungi altri giorni se vuoi ...
        return days;
    }

    private static WorkoutExercise createExercise(String name, String muscleGroup, int order, int numSeries, int targetReps, double targetWeight) {
        int fakeBaseId = name.hashCode();
        WorkoutExercise workoutExercise = new WorkoutExercise(fakeBaseId, name, order);
        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < numSeries; i++) {
            series.add(new Serie(i + 1, targetReps, targetWeight));
        }
        workoutExercise.setSeries(series);
        return workoutExercise;
    }

    public static List<WorkoutExercise> getAvailableExercises() {
        List<WorkoutExercise> available = new ArrayList<>();
        // Usa il costruttore corretto: (baseId, name, order)
        available.add(new WorkoutExercise(1, "Bench Press", 0));
        available.add(new WorkoutExercise(2, "Squat", 0));
        available.add(new WorkoutExercise(3, "Deadlift", 0));
        available.add(new WorkoutExercise(4, "Overhead Press", 0));
        available.add(new WorkoutExercise(5, "Pull-ups", 0));
        available.add(new WorkoutExercise(6, "Dips", 0));
        available.add(new WorkoutExercise(7, "Barbell Rows", 0));
        available.add(new WorkoutExercise(8, "Bicep Curls", 0));
        return available;
    }

}
