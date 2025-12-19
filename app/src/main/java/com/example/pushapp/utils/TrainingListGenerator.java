package com.example.pushapp.utils;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.ExerciseSeries;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;

import java.util.ArrayList;

public class TrainingListGenerator {
    //generates a list of workout plans
    public static ArrayList<Training> generateTrainingList(){
        ArrayList<Training> trainings = new ArrayList<>();

        trainings.add(new Training(0, "Push Pull Legs", "Classic split"));
        trainings.add(new Training(1, "Bro Split", "Terrible Split"));
        trainings.add(new Training(2, "Push Pull Push Pull", "Great split"));
        trainings.add(new Training(3, "Everything but legs", "Best split"));
        trainings.add(new Training(4, "Milkshake", "Banana split"));

        // generates workout days for each training
        for(int i = 0; i < trainings.size(); i++){
            ArrayList<TrainingDay> workouts = new ArrayList<>();
            for(int j = 0; j < 5; j++) {
                workouts.add(new TrainingDay(i * 10 + j, trainings.get(i).getName() + j, generateExerciseSeries()));
            }
            trainings.get(i).setTrainingDaysList(workouts);
        }

        return trainings;
    }

    public static ArrayList<ExerciseSeries> generateExerciseSeries (){
        // 5 esercizi
        Exercise[] es = generateExercises();

        ArrayList<ExerciseSeries> exercises = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            exercises.add(new ExerciseSeries(i, generateSeriesReps(), es[i], 90));
        }

        return exercises;

    }

    public static ArrayList<ExerciseSeries.SeriesReps> generateSeriesReps (){
        ArrayList<ExerciseSeries.SeriesReps> series = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            series.add(new ExerciseSeries.SeriesReps(8, 100));
        }
        return series;


    }

    public static Exercise[] generateExercises(){
        Exercise[] exercises = new Exercise[5];
        exercises[0] = new Exercise("Pushups", 0);
        exercises[1] = new Exercise("Pullups", 1);
        exercises[2] = new Exercise("Bench Press", 2);
        exercises[3] = new Exercise("Deadlift", 3);
        exercises[4] = new Exercise("Squats", 4);
        return exercises;
    }
}
