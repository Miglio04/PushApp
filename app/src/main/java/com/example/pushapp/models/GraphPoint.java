package com.example.pushapp.models;

/**
 * Data model representing a single point on a graph.
 * Used for plotting workout statistics over time.
 */
public class GraphPoint {
    public long date;
    public float value;

    public GraphPoint() {
    }

    /**
     * Constructs a GraphPoint with specific values.
     *
     * @param date  The timestamp of the data point.
     * @param value The value (e.g., volume or weight) at that time.
     */
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