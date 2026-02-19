package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Data source for handling local session checks.
 * Wraps the FirebaseAuth instance to determine if a user is currently logged in locally.
 */
public class SessionLocalDataSource {
    private final FirebaseAuth auth;

    SessionLocalDataSource() {
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Retrieves the currently logged-in user from the authentication instance.
     *
     * @return A SessionUser object if a user is logged in, null otherwise.
     */
    public SessionUser getCurrentSessionUser(){
        if(auth.getCurrentUser() != null) {
            return new SessionUser(auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail());
        }else{
            return null;
        }
    }
}