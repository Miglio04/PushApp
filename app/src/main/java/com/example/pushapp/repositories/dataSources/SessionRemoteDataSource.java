package com.example.pushapp.repositories.dataSources;

import android.widget.Toast;

import com.example.pushapp.repositories.SessionCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SessionRemoteDataSource {
    private SessionCallback callback = null;
    private final FirebaseAuth mAuth;

    public SessionRemoteDataSource(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void setCallback(SessionCallback callback){
        this.callback = callback;
    }

    public void signInWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        callback.onSuccessFromLogin(task.getResult().getUser().getUid());
                    } else {
                        callback.onFailureFromLogin(task.getException());
                    }
                });
    }
}
