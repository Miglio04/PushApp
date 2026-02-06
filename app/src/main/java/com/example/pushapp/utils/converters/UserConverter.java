package com.example.pushapp.utils.converters;

import com.example.pushapp.models.User;
import com.example.pushapp.models.firebaseModels.FirebaseUser;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class UserConverter {
    public static FirebaseUser userToFirebaseUser(User user) {
        if (user == null) return null;

        FirebaseUser firebaseUser = new FirebaseUser();

        firebaseUser.setUserId(user.getUserId());
        firebaseUser.setName(user.getName());
        firebaseUser.setSurname(user.getSurname());
        firebaseUser.setEmail(user.getEmail());
        firebaseUser.setGender(user.getGender());
        firebaseUser.setAge(user.getAge());
        firebaseUser.setHeight(user.getHeight());
        firebaseUser.setWeight(user.getWeight());
        firebaseUser.setGoalWeight(user.getGoalWeight());

        firebaseUser.setWeightProgress(user.getWeightProgress() != null ?
                user.getWeightProgress() : new ArrayList<>());

        firebaseUser.setTrainingPlans(user.getTrainingPlans() != null ?
                user.getTrainingPlans() : new ArrayList<>());

        firebaseUser.setCurrentTrainingPlan(user.getCurrentTrainingPlan());

        if (user.getCreatedAt() != null) {
            firebaseUser.setCreatedAt(user.getCreatedAt());
        }

        return firebaseUser;
    }

    public static User firebaseUserToUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        User user = new User(firebaseUser.getUserId(), firebaseUser.getEmail());

        user.setName(firebaseUser.getName());
        user.setSurname(firebaseUser.getSurname());
        user.setGender(firebaseUser.getGender());
        user.setAge(firebaseUser.getAge());
        user.setHeight(firebaseUser.getHeight());
        user.setWeight(firebaseUser.getWeight());
        user.setGoalWeight(firebaseUser.getGoalWeight());
        user.setWeightProgress(firebaseUser.getWeightProgress());
        user.setTrainingPlans(firebaseUser.getTrainingPlans());
        user.setCurrentTrainingPlan(firebaseUser.getCurrentTrainingPlan());

        if (firebaseUser.getCreatedAt() != null) {
            user.setCreatedAt(firebaseUser.getCreatedAt());
        }

        return user;
    }
}
