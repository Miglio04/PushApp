package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;

public class SessionRepository implements SessionCallback {
    private final SessionLocalDataSource sessionLocalDataSource;
    private final SessionRemoteDataSource sessionRemoteDataSource;


    // provvisorio: bisognerà creare una classe di modello "session"
    private final MutableLiveData<Result> activeUserIdLiveData = new MutableLiveData<>();

    SessionRepository(SessionLocalDataSource sessionLocalDataSource, SessionRemoteDataSource sessionRemoteDataSource) {
        this.sessionLocalDataSource = sessionLocalDataSource;
        this.sessionRemoteDataSource = sessionRemoteDataSource;
        sessionRemoteDataSource.setCallback(this);
    }

    public MutableLiveData<Result> getActiveUserIdLiveData() {
        return activeUserIdLiveData;
    }

    public void signUpWithEmailAndPassword(String email, String password){

    }

    public void signUpWithGoogle(String idToken){

    }

    public void signInWithEmailAndPassword(String email, String password){
        sessionRemoteDataSource.signInWithEmailAndPassword(email, password);
    }

    public void signInWithGoogle(String idToken){

    }

    public String getCurrentUserId() {
        return sessionLocalDataSource.getCurrentUserId();
    }

    public void forgotPassword() {

    }

    @Override
    public void onSuccessFromLogin(String uid) {
        activeUserIdLiveData.postValue(new Result.SessionSuccess(uid));
    }

    @Override
    public void onFailureFromLogin(Exception e) {
        activeUserIdLiveData.postValue(new Result.Error(e.getMessage()));
    }
}
