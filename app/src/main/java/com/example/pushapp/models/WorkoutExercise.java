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

@Entity(tableName = "workoutExercise",
        foreignKeys = @ForeignKey(
                entity = Routine.class,
                parentColumns = "routineId",
                childColumns = "routineId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("routineId"))

public class WorkoutExercise implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "workoutExerciseId")
    @NonNull
    private int workoutExerciseId;
    @ColumnInfo(name = "routineId")
    private String routineId;
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

    // --- NUOVO CAMPO PER LE ISTRUZIONI ---
    private String instructions;

    // --- CAMPI TRANSIENTI (NON SALVATI SU FIREBASE) ---
    @Ignore
    @Exclude
    private boolean isExpanded = false;
    @Ignore
    @Exclude
    private String muscleGroup;

    // Costruttore vuoto per Firebase
    public WorkoutExercise() {
        this.series = new ArrayList<>();
        this.name = "";
        this.notes = "";
        this.instructions = ""; // Inizializza vuoto per sicurezza
    }

    // Costruttore per creare un nuovo esercizio a partire da un esercizio base dell'API
    public WorkoutExercise(int workoutExerciseId, String name, int order) {
        this.workoutExerciseId = workoutExerciseId;
        this.name = name;
        this.order = order;
        this.series = new ArrayList<>();
        this.instructions = ""; // Inizializza vuoto
    }

    // --- GETTERS E SETTERS ---
    public int getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(int workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }
    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }
    @Exclude
    @Ignore
    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    @Ignore
    public void setSeries(List<Serie> series) { this.series = series; }
    public void addSerie(Serie serie) {
        getSeries().add(serie);
    }



    // --- GETTER E SETTER PER LE ISTRUZIONI ---
    public String getInstructions() { return instructions != null ? instructions : ""; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

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