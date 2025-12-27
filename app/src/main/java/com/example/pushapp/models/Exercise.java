package com.example.pushapp.models;

import java.io.Serializable;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;

public class Exercise implements Serializable {
    private int baseExerciseId;
    private String name;
    private int order;
    private List<Serie> series;
    private String notes;
    private int restTimeIndex = 2;  // Default index (90s)

    // --- CAMPI TRANSIENTI (NON SALVATI SU FIREBASE) ---
    @Exclude
    private boolean isExpanded = false;
    @Exclude
    private String muscleGroup; // Verrà popolato dopo aver caricato i dettagli dall'API

    // Costruttore vuoto per Firebase
    public Exercise() {
        this.series = new ArrayList<>();
        this.name = "";
        this.notes = "";
    }

    // Costruttore per creare un nuovo esercizio a partire da un esercizio base dell'API
    public Exercise(int baseExerciseId, String name, int order) {
        this.baseExerciseId = baseExerciseId;
        this.name = name;
        this.order = order;
        this.series = new ArrayList<>();
    }

    // --- GETTERS E SETTERS ---

    public int getBaseExerciseId() { return baseExerciseId; }
    public void setBaseExerciseId(int baseExerciseId) { this.baseExerciseId = baseExerciseId; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    public void setSeries(List<Serie> series) { this.series = series; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }

    public void addSerie(Serie serie) {
        getSeries().add(serie);;
    }

    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }

    @Exclude
    public boolean isExpanded() { return isExpanded; }
    @Exclude
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    @Exclude
    public String getMuscleGroup() { return muscleGroup; }
    @Exclude
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
}
