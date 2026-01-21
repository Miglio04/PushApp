package com.example.pushapp.repositories;

public interface SessionCallback {
    void onSuccessFromLogin(String uid);
    void onFailureFromLogin(Exception e);
}
