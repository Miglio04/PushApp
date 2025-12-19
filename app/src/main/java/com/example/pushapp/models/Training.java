package com.example.pushapp.models;

import java.util.ArrayList;

public class Training {
    private int id;

    private String name;

    private String description;

    public ArrayList<TrainingDay> getTrainingDaysList() {
        return TrainingDaysList;
    }

    public void setTrainingDaysList(ArrayList<TrainingDay> trainingDaysList) {
        this.TrainingDaysList = trainingDaysList;
    }

    private ArrayList<TrainingDay> TrainingDaysList;


    public Training(int id, String name, String description){
        setId(id);
        setName(name);
        setDescription(description);
    }

    public Training(int id, String name, String description, ArrayList<TrainingDay> TrainingDaysList){
        setId(id);
        setName(name);
        setDescription(description);
        setTrainingDaysList(TrainingDaysList);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
