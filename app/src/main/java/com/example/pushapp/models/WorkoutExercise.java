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
import java.util.UUID;

/**
 * Represents an exercise within a workout routine.
 * Stores configuration like order, rest times, and associated series.
 * This entity is used for both local Room database storage and Firestore synchronization.
 */
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
    private String workoutExerciseId;

    /** Timestamp of creation in milliseconds. */
    @ColumnInfo(name = "createdAt")
    private long createdAt;

    /** ID of the user who owns this exercise configuration. */
    @ColumnInfo(name = "userId")
    private String userId;

    /** ID of the parent routine this exercise belongs to. */
    @ColumnInfo(name = "routineId")
    private String routineId;

    /** Name of the exercise. Note: Ensure this matches Firestore field name or use @PropertyName if different. */
    @ColumnInfo(name = "exerciseName")
    private String exerciseName;

    /** Index representing the rest time duration (e.g., 0=30s, 1=60s...). Default is 2. */
    @ColumnInfo(name = "restTimeIndex")
    private int restTimeIndex = 2;

    /** List of series (sets) for this exercise. Ignored by Room (stored in separate table). */
    @Ignore
    private List<Serie> series;

    /** UI state for expansion in lists. Excluded from Firestore. */
    @Ignore
    @Exclude
    private boolean isExpanded = false;

    /** Muscle group associated with the exercise. UI helper, not persisted. */
    @Ignore
    @Exclude
    private String muscleGroup;

    /**
     * Default constructor.
     * Generates a unique ID and initializes series list.
     */
    public WorkoutExercise() {
        this.workoutExerciseId = UUID.randomUUID().toString();
        this.series = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Constructs a new WorkoutExercise with a name.
     *
     * @param exerciseName The name of the exercise.
     */
    public WorkoutExercise(String exerciseName) {
        this.workoutExerciseId = UUID.randomUUID().toString();
        this.exerciseName = exerciseName;
        this.series = new ArrayList<>();
    }

    /**
     * Getters and Setters
     */
    public String getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(String workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getExerciseName() { return exerciseName != null ? exerciseName : ""; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public int getRestTimeIndex() { return restTimeIndex; }
    public void setRestTimeIndex(int restTimeIndex) { this.restTimeIndex = restTimeIndex; }

    public List<Serie> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
    public void setSeries(List<Serie> series) { this.series = series; }


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