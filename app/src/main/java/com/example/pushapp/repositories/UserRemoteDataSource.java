package com.example.pushapp.repositories;

import android.util.Log;

import com.example.pushapp.models.User;
import com.example.pushapp.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRemoteDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private UserCallback userCallback = null;

    UserRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void setUserCallback(UserCallback userCallback) {
        this.userCallback = userCallback;
    }

    public void fetchCurrentUser(){
        String userId = auth.getCurrentUser().getUid();
    }

    public void fetchUserById(String uId) {
        db.collection(Constants.COLLECTION_USERS).document(uId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            userCallback.onSuccessFromRemote(user);
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

    //to implement
    public void insertUser(User user){
        // insert user in Firestore.
        // callback
    }
}
