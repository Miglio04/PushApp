package com.example.pushapp.utils;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;

import java.util.ArrayList;
import java.util.List;

public class TrainingListGenerator {

    public static ArrayList<Training> generateTrainingList() {
        ArrayList<Training> trainings = new ArrayList<>();
        Training ppl = new Training("Push Pull Legs", "Split classico PPL");
        ppl.setTrainingDaysList(generatePPLDays());
        trainings.add(ppl);
        return trainings;
    }

    private static ArrayList<TrainingDay> generatePPLDays() {
        ArrayList<TrainingDay> days = new ArrayList<>();
        TrainingDay pushDay = new TrainingDay("Push Day", 1);
        pushDay.addExercise(createExercise("Bench Press", "Petto", 1, 4, 8, 80));
        pushDay.addExercise(createExercise("Overhead Press", "Spalle", 2, 3, 10, 40));
        days.add(pushDay);
        // ... aggiungi altri giorni se vuoi ...
        return days;
    }

    private static Exercise createExercise(String name, String muscleGroup, int order, int numSeries, int targetReps, double targetWeight) {
        int fakeBaseId = name.hashCode();
        Exercise exercise = new Exercise(fakeBaseId, name, order);
        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < numSeries; i++) {
            series.add(new Serie(i + 1, targetReps, targetWeight));
        }
        exercise.setSeries(series);
        return exercise;
    }

    public static List<Exercise> getAvailableExercises() {
        List<Exercise> available = new ArrayList<>();
        // Usa il costruttore corretto: (baseId, name, order)
        available.add(new Exercise(1, "Bench Press", 0));
        available.add(new Exercise(2, "Squat", 0));
        available.add(new Exercise(3, "Deadlift", 0));
        available.add(new Exercise(4, "Overhead Press", 0));
        available.add(new Exercise(5, "Pull-ups", 0));
        available.add(new Exercise(6, "Dips", 0));
        available.add(new Exercise(7, "Barbell Rows", 0));
        available.add(new Exercise(8, "Bicep Curls", 0));
        return available;
    }

}
