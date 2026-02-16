package com.example.pushapp.models.api;

import com.google.gson.annotations.SerializedName;

public class ExerciseApiModel {

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("muscle")
    private String muscle;

    @SerializedName("equipment")
    private String equipment;
    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("instructions")
    private String instructions;

    public ExerciseApiModel() { }

    public ExerciseApiModel(String name, String type, String muscle, String equipment, String difficulty, String instructions) {
        this.name = name;
        this.type = type;
        this.muscle = muscle;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.instructions = instructions;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getMuscle() { return muscle; }
    public String getEquipment() { return equipment; }
    public String getDifficulty() { return difficulty; }
    public String getInstructions() { return instructions; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMuscle(String muscle) { this.muscle = muscle; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}