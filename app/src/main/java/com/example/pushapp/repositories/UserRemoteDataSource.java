package com.example.pushapp.repositories;

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
}
