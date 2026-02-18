package com.example.pushapp.repositories;

/**
 * Generic callback interface for handling asynchronous Firebase/remote operations.
 * Should be used to return a result of type T upon success or an exception upon failure.
 *
 * @param <T> The type of the result object returned on success.
 */
public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
