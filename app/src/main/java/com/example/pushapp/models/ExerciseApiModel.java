package com.example.pushapp.models;

public class ExerciseApiModel {
    private String name;
    private String type;
    private String muscle;
    private String equipment;
    private String difficulty;
    private String instructions;

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMuscle() { return muscle; }
    public String getEquipment() { return equipment; }
    public String getDifficulty() { return difficulty; }
    public String getInstructions() { return instructions; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMuscle(String muscle) { this.muscle = muscle; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
