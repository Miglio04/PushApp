package com.example.pushapp.utils.converters;

import com.example.pushapp.models.User;
import com.example.pushapp.models.firebaseModels.FirebaseUser;

/**
 * Utility class for converting between local User domain models and FirebaseUser DTOs.
 * Facilitates mapping of user data for Firestore operations.
 */
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

        if (firebaseUser.getCreatedAt() != null) {
            user.setCreatedAt(firebaseUser.getCreatedAt());
        }

        return user;
    }
}
