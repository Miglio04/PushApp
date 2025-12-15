package com.example.pushapp.utils;

import java.util.ArrayList;
import java.util.List;

public class WorkoutCard {
    // Title description e image sono final al momento, se e quando verrà aggiunta la possibilità di
    // modificare questi campi andranno rimossi il final e aggiunti i setter
    private final String title;
    private final String description;
    private final int imageResId;
    private String note;
    private List<WorkoutSet> sets;
    private int restTimeSeconds;

    public WorkoutCard(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.note = "";
        this.sets = new ArrayList<>();
        this.restTimeSeconds = 120;
    }

    // Setter commentati, vedi primo commento
    public String getTitle() { return title; }
    //public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    //public void setDescription(String description) { this.description = description; }

    public int getImageResId() { return imageResId; }
    //public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<WorkoutSet> getSets() { return sets; }
    public void setSets(List<WorkoutSet> sets) { this.sets = sets; }
    public void addSet(WorkoutSet set) { sets.add(set); }

    public int getRestTimeSeconds() { return restTimeSeconds; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }
}
