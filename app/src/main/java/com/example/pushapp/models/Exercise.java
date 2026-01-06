package com.example.pushapp.models;

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

@Entity(
        tableName = "exercise",
        foreignKeys = @ForeignKey(
                entity = TrainingDay.class,
                parentColumns = "id",
                childColumns = "trainingDayId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("trainingDayId")
)
public class Exercise implements Serializable {
    @PrimaryKey
    private int baseExerciseId;

    public String getTrainingDayId() {
        return trainingDayId;
    }

    public void setTrainingDayId(String trainingDayId) {
        this.trainingDayId = trainingDayId;
    }

    @ColumnInfo(name = "trainingDayId")
    private String trainingDayId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "order")
    private int order;
    @Ignore
    private List<Serie> series;
    @ColumnInfo(name = "notes")
    private String notes;
    @ColumnInfo(name = "restTimeIndex")
    private int restTimeIndex = 2;  // Default index (90s)

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

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Ignore
    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    @Ignore
    public void setSeries(List<Serie> series) { this.series = series; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }

    @Ignore
    public void addSerie(Serie serie) {
        getSeries().add(serie);;
    }

    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }

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
