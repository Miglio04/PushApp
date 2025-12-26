package com.example.pushapp.utils;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
