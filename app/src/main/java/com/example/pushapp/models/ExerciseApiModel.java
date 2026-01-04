package com.example.pushapp.models;

public class ExerciseApiModel {
    private String name;
    private String type;
    private String muscle;
    private String equipment;
    private String difficulty;
    private String instructions;

    // --- COSTRUTTORI ---

    // 1. Costruttore vuoto (Obbligatorio per Firebase/Gson)
    public ExerciseApiModel() {
    }

    // 2. Costruttore completo (Utile per creare dati di test o manuali)
    public ExerciseApiModel(String name, String type, String muscle, String equipment, String difficulty, String instructions) {
        this.name = name;
        this.type = type;
        this.muscle = muscle;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.instructions = instructions;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMuscle() { return muscle; }
    public String getEquipment() { return equipment; }
    public String getDifficulty() { return difficulty; }
    public String getInstructions() { return instructions; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMuscle(String muscle) { this.muscle = muscle; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}