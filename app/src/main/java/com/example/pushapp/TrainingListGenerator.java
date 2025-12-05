package com.example.pushapp;

import java.util.ArrayList;

public class TrainingListGenerator {
    public static ArrayList<Training> generateTrainingList(){
        ArrayList<Training> trainings = new ArrayList<>();

        trainings.add(new Training("Push Pull Legs", "Classic split"));
        trainings.add(new Training("Bro Split", "Terrible Split"));
        trainings.add(new Training("Push Pull Push Pull", "Great split"));
        trainings.add(new Training("Everything but legs", "Best split"));
        trainings.add(new Training("Milkshake", "Banana split"));
        return trainings;
    }
}
