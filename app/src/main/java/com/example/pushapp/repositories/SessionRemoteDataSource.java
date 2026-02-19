package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Data source for handling session-related operations with the remote Firebase Authentication service.
 * Manages sign-in with credentials (e.g., Google), email/password login, registration, and password reset.
 */
public class SessionRemoteDataSource {
    private SessionCallback callback = null;
    private final FirebaseAuth mAuth;

    SessionRemoteDataSource(){
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param callback The callback implementation.
     */
    public void setCallback(SessionCallback callback){
        this.callback = callback;
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    public void signInWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        SessionUser sessionUser = new SessionUser(task.getResult().getUser().getUid(), task.getResult().getUser().getEmail());
                        callback.onSuccessFromLogin(sessionUser);
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        callback.onFailureFromNetwork(task.getException());
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        callback.onUserNotFound(task.getException());
                    } else {
                        callback.onFailureFromLogin(task.getException());
                    }
                });
    }

    /**
     * Registers a new user account with email and password.
     *
     * @param email    The email address for registration.
     * @param password The password for the new account.
     */
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
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        callback.onFailureFromNetwork(task.getException());
                    } else {
                        callback.onFailureFromRegister(task.getException());
                    }
                });
    }

    /**
     * Sends a password reset email to the specified address.
     * Handles cases where the user is not found.
     *
     * @param email The email address to send the reset link to.
     */
    public void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccessFromPasswordReset(email);
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        callback.onUserNotFound(new Exception("User not found"));
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        callback.onFailureFromNetwork(task.getException());
                    } else {
                        callback.onFailureFromPasswordReset(task.getException());
                    }
                });
    }

    /**
     * Signs out the current user from Firebase Authentication.
     */
    public void logout() {
        mAuth.signOut();
    }
}
