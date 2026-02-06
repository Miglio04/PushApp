package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class SessionRepository implements SessionCallback {
    private final SessionLocalDataSource sessionLocalDataSource;
    private final SessionRemoteDataSource sessionRemoteDataSource;
    private final MutableLiveData<Result> sessionLiveData = new MutableLiveData<>();

    SessionRepository(SessionLocalDataSource sessionLocalDataSource, SessionRemoteDataSource sessionRemoteDataSource) {
        this.sessionLocalDataSource = sessionLocalDataSource;
        this.sessionRemoteDataSource = sessionRemoteDataSource;
        sessionRemoteDataSource.setCallback(this);
    }

    public MutableLiveData<Result> getSessionLiveData() {
        return sessionLiveData;
    }

    public void signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        sessionRemoteDataSource.signInWithCredentials(credential);
    }

    public void signInWithEmailAndPassword(String email, String password) {
        sessionRemoteDataSource.signInWithEmailAndPassword(email, password);
    }

    public void registerWithEmailAndPassword(String email, String password) {
        sessionRemoteDataSource.registerWithEmailAndPassword(email, password);
    }

    public void getCurrentUserId() {
        SessionUser sessionUser = sessionLocalDataSource.getCurrentSessionUser();
        if (sessionUser != null) {
            sessionLiveData.postValue(new Result.SessionSuccess(sessionLocalDataSource.getCurrentSessionUser()));
        } else {
            sessionLiveData.postValue(new Result.Error("User not found"));
        }
    }

    public void sendPasswordResetEmail(String email) {
        sessionRemoteDataSource.sendPasswordResetEmail(email);
    }

    public void logout() {
        sessionRemoteDataSource.logout();
    }

    @Override
    public void onSuccessFromLogin(SessionUser sessionUser) {
        sessionLiveData.postValue(new Result.SessionSuccess(sessionUser));
    }

    @Override
    public void onFailureFromLogin(Exception e) {
        sessionLiveData.postValue(new Result.Error.LoginError(e.getMessage()));
    }

    public void onSuccessFromRegister(SessionUser sessionUser) {
        sessionLiveData.setValue(new Result.SessionSuccess(sessionUser));
    }

    public void onFailureFromRegister(Exception e) {
        sessionLiveData.setValue(new Result.Error.RegistrationError(e.getMessage()));
    }

    public void onSuccessFromPasswordReset(String email) {
        sessionLiveData.setValue(new Result.PasswordResetSuccess(email));
    }

    public void onFailureFromPasswordReset(Exception e) {
        sessionLiveData.setValue(new Result.Error.ForgotPasswordError(e.getMessage()));
    }

    public void onUserNotFoundFromPasswordReset(Exception e) {
        sessionLiveData.setValue(new Result.Error.UserNotFound(e.getMessage()));
    }

    public void clearLiveData() {
        sessionLiveData.setValue(null);
    }

}
