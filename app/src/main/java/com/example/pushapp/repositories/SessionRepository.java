package com.example.pushapp.repositories;

import com.example.pushapp.repositories.dataSources.SessionDataSource;

public class SessionRepository {
    private final SessionDataSource sessionDataSource;

    SessionRepository(SessionDataSource sessionDataSource) {
        this.sessionDataSource = sessionDataSource;
    }

    public void signUpWithEmailAndPassword(String email, String password){

    }

    public void signUpWithGoogle(String idToken){

    }

    public void signInWithEmailAndPassword(String email, String password){

    }

    public void signInWithGoogle(String idToken){

    }


    public String getCurrentUserId() {
        return sessionDataSource.getCurrentUserId();
    }

    public void forgotPassoword(){

    }
}
