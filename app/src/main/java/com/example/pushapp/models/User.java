package com.example.pushapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @PrimaryKey
    private String uid;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "surname")
    private String surname;
    @ColumnInfo(name = "gender")
    private String gender;
    @ColumnInfo(name = "age")
    private int age;
    @ColumnInfo(name = "weight")
    private double weight;
    @ColumnInfo(name = "height")
    private int height;
    @ColumnInfo(name = "goalWeight")
    private double goalWeight;
    @ColumnInfo(name = "createdAt")
    private Timestamp createdAt;
    private List<String> trainingPlans;
    private List<Double> weightProgress;

    public User() {
        // Required for Firestore
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public double getGoalWeight() { return goalWeight; }
    public void setGoalWeight(double goalWeight) { this.goalWeight = goalWeight; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<String> getTrainingPlans() {
        if (trainingPlans == null) trainingPlans = new ArrayList<>();
        return trainingPlans;
    }
    public void setTrainingPlans(List<String> trainingPlans) { this.trainingPlans = trainingPlans; }

    public List<Double> getWeightProgress() {
        if (weightProgress == null) weightProgress = new ArrayList<>();
        return weightProgress;
    }
    public void setWeightProgress(List<Double> weightProgress) { this.weightProgress = weightProgress; }
}
