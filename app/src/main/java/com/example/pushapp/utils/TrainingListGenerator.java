package com.example.pushapp.utils;

import com.example.pushapp.models.Training;
import com.example.pushapp.models.Workout;

import java.util.ArrayList;

public class TrainingListGenerator {
    public static ArrayList<Training> generateTrainingList(){
        ArrayList<Training> trainings = new ArrayList<>();

        trainings.add(new Training(0, "Push Pull Legs", "Classic split"));
        trainings.add(new Training(1, "Bro Split", "Terrible Split"));
        trainings.add(new Training(2, "Push Pull Push Pull", "Great split"));
        trainings.add(new Training(3, "Everything but legs", "Best split"));
        trainings.add(new Training(4, "Milkshake", "Banana split"));

        // generates workout days for each training
        for(int i = 0; i < trainings.size(); i++){
            ArrayList<Workout> workouts = new ArrayList<>();
            for(int j = 0; j < 5; j++) {
                workouts.add(new Workout(i * 10 + j, trainings.get(i).getName() + j));
            }
            trainings.get(i).setWorkoutList(workouts);
        }

        return trainings;
    }
}
