package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.FirebaseAuth;

public class SessionLocalDataSource {
    private final FirebaseAuth auth;

    SessionLocalDataSource() {
        this.auth = FirebaseAuth.getInstance();
    }

    public SessionUser getCurrentSessionUser(){
        if(auth.getCurrentUser() != null) {
            return new SessionUser(auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail());
        }else{
            return null;
        }
    }
}
