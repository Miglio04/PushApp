package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a workout routine (e.g., "Leg Day").
 * A routine belongs to a Training plan and contains a list of WorkoutExercises.
 */
@Entity(tableName = "routine",
        foreignKeys = @ForeignKey(
                entity = Training.class,
                parentColumns = "trainingId",
                childColumns = "trainingId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "trainingId")})
public class Routine implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "routineId")
    @NonNull
    private String routineId;
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "trainingId")
    private String trainingId;
    @ColumnInfo(name = "name")
    private String name;
    @Ignore
    private List<WorkoutExercise> workoutExercises;

    /**
     * Default constructor.
     * Generates a new UUID and initializes the exercise list.
     */
    public Routine() {
        this.routineId = UUID.randomUUID().toString();
        this.workoutExercises = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Constructs a new Routine with a name.
     *
     * @param name The name of the routine.
     */
    public Routine(String name) {
        this.routineId = UUID.randomUUID().toString();
        this.name = name;
        this.workoutExercises = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Copy constructor. Creates a new Routine from an existing one.
     *
     * @param routine The routine to copy.
     */
    @Ignore
    public Routine(Routine routine) {
        this.trainingId = routine.getTrainingId();
        this.userId = routine.getUserId();
        this.routineId = routine.getRoutineId();
        this.name = routine.getName();
        if (routine.getWorkoutExercises() != null) {
            this.workoutExercises = new ArrayList<>(routine.getWorkoutExercises());
        } else {
            this.workoutExercises = new ArrayList<>();
        }
        this.createdAt = routine.getCreatedAt();
    }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { if (routineId != null) { this.routineId = routineId; } }

    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Exclude
    @Ignore
    public List<WorkoutExercise> getWorkoutExercises() {
        if (workoutExercises == null) workoutExercises = new ArrayList<>();
        return workoutExercises;
    }
    @Ignore
    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) { this.workoutExercises = workoutExercises; }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
