package com.example.pushapp.utils;

import com.example.pushapp.models.Training;

import java.util.ArrayList;

public class TrainingListGenerator {
    public static ArrayList<Training> generateTrainingList(){
        ArrayList<Training> trainings = new ArrayList<>();

        trainings.add(new Training(0, "Push Pull Legs", "Classic split"));
        trainings.add(new Training(1, "Bro Split", "Terrible Split"));
        trainings.add(new Training(2, "Push Pull Push Pull", "Great split"));
        trainings.add(new Training(3, "Everything but legs", "Best split"));
        trainings.add(new Training(4, "Milkshake", "Banana split"));
        return trainings;
    }
}
