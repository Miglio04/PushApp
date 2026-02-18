package com.example.pushapp.repositories;

import com.example.pushapp.models.User;

/**
 * Callback interface for handling User repository operations.
 * Used to communicate the results of user data operations (insert, fetch, update)
 * from both local and remote data sources.
 */
public interface UserCallback {
    void onSuccessFromLocalInsert(User user);
    void onFailureFromLocal(Exception exception);
    void onSuccessFromRemoteFetch(User user);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocalGet(User user, String userId);
    void onSuccessFromLocalUpdate(User user);
}
