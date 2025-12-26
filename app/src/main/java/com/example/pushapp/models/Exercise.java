package com.example.pushapp.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
    private String id;
    private String name;
    private String muscleGroup;
    private int order;
    private List<Serie> series;
    private String notes;
    @Exclude
    private boolean isExpanded;

    public Exercise() {
        this.series = new ArrayList<>();
    }

    public Exercise(String name, String muscleGroup, int order) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.order = order;
        this.series = new ArrayList<>();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public List<Serie> getSeries() { return series; }
    public void setSeries(List<Serie> series) { this.series = series; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Exclude
    public boolean isExpanded() { return isExpanded; }

    @Exclude
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    public void addSerie(Serie serie) {
        this.series.add(serie);
    }
}
