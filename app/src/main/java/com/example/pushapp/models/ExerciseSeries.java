package com.example.pushapp.models;

import java.util.ArrayList;

public class ExerciseSeries {
    private int id;
    private ArrayList<SeriesReps> reps;
    private Exercise exercise;
    private int restTime;

    public ExerciseSeries(int id, ArrayList<SeriesReps> reps, Exercise exercise, int restTime) {
        this.id = id;
        this.reps = reps;
        this.exercise = exercise;
        this.restTime = restTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<SeriesReps> getReps() {
        return reps;
    }

    public void setReps(ArrayList<SeriesReps> reps) {
        this.reps = reps;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }


    public static class SeriesReps{
        private int reps;
        private int weight;

        public SeriesReps(int reps, int weight) {
            setReps(reps);
            setWeight(weight);
        }

        public int getReps() {
            return reps;
        }

        public void setReps(int reps) {
            this.reps = reps;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }


}
