package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.AuthCredential;
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
     * Authenticates a user using an AuthCredential (e.g., Google Sign-In).
     * Determines if the user is new or existing and triggers the appropriate callback.
     *
     * @param credential The authentication credential to verify.
     */
    public void signInWithCredentials(AuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            SessionUser sessionUser = new SessionUser(firebaseUser.getUid(), firebaseUser.getEmail());
                            if(isNewUser) {
                                callback.onSuccessFromRegister(sessionUser);
                            } else{
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
                        callback.onUserNotFoundFromPasswordReset(new Exception("User not found"));
                    } else {
                        callback.onFailureFromPasswordReset(task.getException());
                    }
                });
    }

    /**
     * Attempts to login with Google credentials (login only, not registration).
     * If the user is new (not registered), triggers onGoogleUserNotRegistered callback.
     *
     * @param credential The Google authentication credential.
     */
    public void loginOnlyWithCredentials(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            SessionUser sessionUser = new SessionUser(firebaseUser.getUid(), firebaseUser.getEmail());
                            if (isNewUser) {
                                firebaseUser.delete();
                                callback.onGoogleUserNotRegistered(sessionUser);
                            } else {
                                callback.onSuccessFromLogin(sessionUser);
                            }
                        } else {
                            callback.onFailureFromLogin(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailureFromLogin(task.getException());
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
