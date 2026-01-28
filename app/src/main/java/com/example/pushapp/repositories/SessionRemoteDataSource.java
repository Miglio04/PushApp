package com.example.pushapp.repositories;
import android.widget.Toast;

import com.example.pushapp.models.SessionUser;
import com.example.pushapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SessionRemoteDataSource {
    private SessionCallback callback = null;
    private final FirebaseAuth mAuth;

    SessionRemoteDataSource(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void setCallback(SessionCallback callback){
        this.callback = callback;
    }

    public void signInWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        SessionUser sessionUser = new SessionUser(task.getResult().getUser().getUid(), task.getResult().getUser().getEmail());
                        callback.onSuccessFromLogin(sessionUser);
                    } else {
                        callback.onFailureFromLogin(task.getException());
                    }
                });
    }

    public void registerWithEmailAndPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            SessionUser sessionUser = new SessionUser(firebaseUser.getUid(), firebaseUser.getEmail());
                            callback.onSuccessFromRegister(sessionUser);
                        } else {
                            callback.onFailureFromRegister(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailureFromRegister(task.getException());
                    }
                });
    }
}
