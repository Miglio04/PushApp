package com.example.pushapp.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.SessionUser;
import com.example.pushapp.models.User;

public class UserRepository implements UserCallback {
    private final static String TAG = "UserRepository";
    private final UserLocalDataSource localDataSource;
    private final UserRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> currentUser;

    UserRepository(UserLocalDataSource localDataSource, UserRemoteDataSource remoteDataSource, SessionRepository sessionRepository) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setUserCallback(this);
        this.remoteDataSource.setUserCallback(this);
        this.currentUser = new MutableLiveData<>();
    }

    //  returns the reference of the liveData
    public MutableLiveData<Result> getCurrentUser() {
        return currentUser;
    }

    // provvisorio: prende sempre i dati da Firebase
    public void fetchUserById(SessionUser user){
        remoteDataSource.fetchUserById(user.getUserId());
    }

    public void insertUser(User user){
        localDataSource.insertUser(user);
        Log.d(TAG, "Inserting user in local database");
    }

    public void onSuccessFromLocal(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        Log.d(TAG, "Local database operation successful");
        // method not implemented yet
        remoteDataSource.insertUser(user);
    }
    public void onFailureFromLocal(Exception exception){
        currentUser.postValue(new Result.Error.LocalDatabaseError(exception.getMessage()));
    }
    //temporary version of method: not considering versioning and user's local storage
    public void onSuccessFromRemote(User user){
        currentUser.postValue(new Result.UserSuccess(user));
    }
    //provvisorio: quando verrà implementato il workmanager dovrà ritentare la richiesta
    public void onFailureFromRemote(Exception exception){
        currentUser.postValue(new Result.Error(exception.getMessage()));
    }
}
