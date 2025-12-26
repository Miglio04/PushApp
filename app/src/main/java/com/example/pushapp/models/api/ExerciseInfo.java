package com.example.pushapp.models.api;

// Semplice classe POJO per mappare la risposta dell'API WGER.
// In una implementazione reale con GSON, i nomi dei campi
// dovrebbero corrispondere a quelli del JSON.
public class ExerciseInfo {
    private int id;
    private String name;
    // Aggiungi altri campi dall'API se necessario (es. description, category)

    public ExerciseInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
}
