package com.example.pushapp.repositories;

import com.example.pushapp.models.User;

public interface UserCallback {
    void onSuccessFromLocalInsert(User user);
    void onFailureFromLocal(Exception exception);
    void onSuccessFromRemoteFetch(User user);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocalGet(User user, String userId);
    void onSuccessFromLocalUpdate(User user);
}
