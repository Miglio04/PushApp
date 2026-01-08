package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "exercise",
        foreignKeys = @ForeignKey(
                entity = TrainingDay.class,
                parentColumns = "trainingDayId",
                childColumns = "trainingDayId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("trainingDayId"))

public class Exercise implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "baseExerciseId")
    @NonNull
    private int baseExerciseId;
    @ColumnInfo(name = "trainingDayId")
    private String trainingDayId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "exerciseOrder")
    private int order;
    @ColumnInfo(name = "notes")
    private String notes;
    @ColumnInfo(name = "restTimeIndex")
    private int restTimeIndex = 2;  // Default index (90s)
    @Ignore
    private List<Serie> series;

    // --- CAMPI TRANSIENTI (NON SALVATI SU FIREBASE) ---
    @Ignore
    @Exclude
    private boolean isExpanded = false;
    @Ignore
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
    public String getTrainingDayId() { return trainingDayId; }
    public void setTrainingDayId(String trainingDayId) { this.trainingDayId = trainingDayId; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }

    @Ignore
    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    @Ignore
    public void setSeries(List<Serie> series) { this.series = series; }
    @Ignore
    public void addSerie(Serie serie) { getSeries().add(serie);; }
    @Ignore
    @Exclude
    public boolean isExpanded() { return isExpanded; }
    @Ignore
    @Exclude
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
    @Ignore
    @Exclude
    public String getMuscleGroup() { return muscleGroup; }
    @Ignore
    @Exclude
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
}
