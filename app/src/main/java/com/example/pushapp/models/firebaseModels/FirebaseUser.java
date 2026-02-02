package com.example.pushapp.models.firebaseModels;

import java.util.List;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class FirebaseUser {
    private String userId;
    private String name;
    private String surname;
    private String email;
    private String gender;
    private int height;
    private int age;
    private Double weight;
    private Double goalWeight;
    private List<Double> weightProgress;
    private String currentTrainingPlan;
    private List<String> trainingPlans;
    @ServerTimestamp
    private Timestamp createdAt;

    public FirebaseUser() {
    }

    // Getter e Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public List<Double> getWeightProgress() {
        return weightProgress;
    }

    public void setWeightProgress(List<Double> weightProgress) {
        this.weightProgress = weightProgress;
    }

    public String getCurrentTrainingPlan() {
        return currentTrainingPlan;
    }

    public void setCurrentTrainingPlan(String currentTrainingPlan) {
        this.currentTrainingPlan = currentTrainingPlan;
    }

    public List<String> getTrainingPlans() {
        return trainingPlans;
    }

    public void setTrainingPlans(List<String> trainingPlans) {
        this.trainingPlans = trainingPlans;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
