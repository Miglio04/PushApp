package com.example.pushapp.models;

public class GraphPoint {
    public long date;
    public float value;

    public GraphPoint() {
    }

    public GraphPoint(long date, float value) {
        this.date = date;
        this.value = value;
    }

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