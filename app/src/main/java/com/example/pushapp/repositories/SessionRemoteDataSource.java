package com.example.pushapp.repositories;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
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

    //usato da google per sign in
    public void signInWithCredentials(AuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            SessionUser sessionUser = new SessionUser(firebaseUser.getUid(), firebaseUser.getEmail());
                            Log.e("SessionRemoteDataSource", "isNewUser: " + isNewUser);
                            if(isNewUser) {
                                Log.e("SessionRemoteDataSource", "Registering with google: ");
                                callback.onSuccessFromRegister(sessionUser);
                            } else{
                                Log.e("SessionRemoteDataSource", "Logging in with google: ");
                                callback.onSuccessFromLogin(sessionUser);
                            }
                        }
                        else {
                            callback.onFailureFromRegister(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailureFromRegister(task.getException());
                    }
                });
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

    public void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccessFromPasswordReset(email);
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        callback.onUserNotFoundFromPasswordReset(new Exception("User not found"));
                    } else {
                        callback.onFailureFromPasswordReset(task.getException());
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }
}
