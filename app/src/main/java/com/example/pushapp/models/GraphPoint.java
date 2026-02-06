package com.example.pushapp.models;

public class GraphPoint {
    public long date;   // Timestamp dell'allenamento (asse X)
    public float value; // Valore (asse Y)

    // Costruttore vuoto per Room
    public GraphPoint() {
    }

    // Costruttore completo
    public GraphPoint(long date, float value) {
        this.date = date;
        this.value = value;
    }

    // --- Getters e Setters ---
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}