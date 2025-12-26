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
        Exercise exercise = new Exercise(name, muscleGroup, order);
        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < numSeries; i++) {
            series.add(new Serie(i + 1, targetReps, targetWeight));
        }
        exercise.setSeries(series);
        return exercise;
    }

    public static List<Exercise> getAvailableExercises() {
        List<Exercise> available = new ArrayList<>();
        available.add(new Exercise("Bench Press", "Petto", 0));
        available.add(new Exercise("Squat", "Gambe", 0));
        available.add(new Exercise("Deadlift", "Schiena/Gambe", 0));
        available.add(new Exercise("Overhead Press", "Spalle", 0));
        available.add(new Exercise("Pull-ups", "Dorso", 0));
        available.add(new Exercise("Dips", "Tricipiti/Petto", 0));
        available.add(new Exercise("Barbell Rows", "Dorso", 0));
        available.add(new Exercise("Bicep Curls", "Bicipiti", 0));
        // Aggiungi qui tutti gli altri esercizi che vuoi rendere disponibili
        return available;
    }

}
