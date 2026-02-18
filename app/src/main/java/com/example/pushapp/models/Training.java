package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a Training plan (e.g., "PPL Split", "Full Body").
 * Contains a list of Routines.
 */
@Entity(tableName = "training")
public class Training implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "trainingId")
    private String trainingId;
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "description")
    private String description;
    @Ignore
    private List<Routine> routinesList;

    /**
     * Default constructor required for Firebase and serialization.
     * Initializes identifiers and timestamps.
     */
    public Training() {
        this.trainingId = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.description = "";
        this.name = "";
        this.routinesList = new ArrayList<>();
    }

    /**
     * Constructs a new Training plan with details.
     *
     * @param name        The name of the training plan.
     * @param description A description of the plan.
     */
    @Ignore
    public Training(String name, String description) {
        this.trainingId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.routinesList = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Training(String name, String description, ArrayList<Routine> routinesList) {
        this.trainingId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.routinesList = routinesList != null ? routinesList : new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Exclude
    @Ignore
    public List<Routine> getRoutinesList() { return routinesList; }
    @Ignore
    public void setRoutinesList(ArrayList<Routine> routinesList) {
        this.routinesList = routinesList;
    }
}
