package com.example.pushapp.repositories;

import android.util.Log;

import com.example.pushapp.models.User;
import com.example.pushapp.models.firebaseModels.FirebaseUser;
import com.example.pushapp.utils.converters.UserConverter;
import com.example.pushapp.utils.Constants;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserRemoteDataSource {
    private final FirebaseFirestore db;
    private UserCallback userCallback = null;

    UserRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }
    public void setUserCallback(UserCallback userCallback) {
        this.userCallback = userCallback;
    }

    public void fetchUserById(String uId) {
        db.collection(Constants.COLLECTION_USERS).document(uId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseUser firebaseUser = documentSnapshot.toObject(FirebaseUser.class);
                        if (firebaseUser != null) {
                            User user = UserConverter.firebaseUserToUser(firebaseUser);
                            userCallback.onSuccessFromRemoteFetch(user);
                        }
                    }
                    else{
                        userCallback.onFailureFromRemote(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    userCallback.onFailureFromRemote(new Exception("Error fetching user"));
                });
    }
    public void insertUser(User user) {
        if (user == null || user.getUserId() == null) {
            userCallback.onFailureFromRemote(new Exception("null user or userId"));
        }
        else{
            FirebaseUser firebaseUser = UserConverter.userToFirebaseUser(user);

            db.collection(Constants.COLLECTION_USERS)
                    .document(user.getUserId())
                    .set(firebaseUser)
                    .addOnFailureListener(e -> {
                        if (userCallback != null) {
                            userCallback.onFailureFromRemote(e);
                        }
                    });
        }
    }
}
