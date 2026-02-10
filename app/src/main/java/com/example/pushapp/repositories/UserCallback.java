package com.example.pushapp.repositories;

import com.example.pushapp.models.User;

public interface UserCallback {
    void onSuccessFromLocalInsert(User user);
    void onFailureFromLocal(Exception exception);
    void onSuccessFromRemote(User user);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocalGet(User user);
    void onSuccessFromLocalUpdate(User user);
}
