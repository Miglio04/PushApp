package com.example.pushapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class Exercise {
    @PrimaryKey
    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "muscle")
    private String muscle;

    @ColumnInfo(name = "difficulty")
    private String difficulty;

    public Exercise(@NonNull String name, String muscle, String difficulty) {
        this.name = name;
        this.muscle = muscle;
        setDifficulty(difficulty);
    }

    @NonNull
    public String getName() { return name; }
    public String getMuscle() { return muscle; }
    public String getDifficulty() { return difficulty; }

    public void setName(@NonNull String name) { this.name = name; }
    public void setMuscle(String muscle) { this.muscle = muscle; }
    public void setDifficulty(String difficulty) {
        if(difficulty != null && (difficulty.equals("beginner") || difficulty.equals("intermediate") || difficulty.equals("expert"))) {
            this.difficulty = difficulty;
        }
    }
}
