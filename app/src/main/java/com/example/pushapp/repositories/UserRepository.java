package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.repositories.dataSources.SessionDataSource;
import com.example.pushapp.repositories.dataSources.UserLocalDataSource;
import com.example.pushapp.repositories.dataSources.UserRemoteDataSource;

public class UserRepository implements UserCallback {
    private final SessionDataSource sessionDataSource;
    private final UserLocalDataSource localDataSource;
    private final UserRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> currentUser;

    public UserRepository(UserLocalDataSource localDataSource, UserRemoteDataSource remoteDataSource, SessionDataSource sessionDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.sessionDataSource = sessionDataSource;
        this.sessionDataSource.setUserCallback(this);
        this.localDataSource.setUserCallback(this);
        this.remoteDataSource.setUserCallback(this);
        this.currentUser = new MutableLiveData<>();
    }

    //  returns the reference of the liveData
    public MutableLiveData<Result> getCurrentUser() {
        return currentUser;
    }

    // calls the update of the liveData
    // temporary version of method: not considering versioning and user's local storage
    public void fetchCurrentUser() {
        String userId = sessionDataSource.getCurrentUserId();
        if(userId == null){
            remoteDataSource.fetchCurrentUser();
        }
    }

    // to implement
    public void onSuccessFromLocal(User user){
    }
    //to implement
    public void onFailureFromLocal(Exception exception){

    }
    //temporary version of method: not considering versioning and user's local storage
    public void onSuccessFromRemote(User user){
        this.currentUser.postValue(new Result.UserSuccess(user));
    }
    // to implement
    public void onFailureFromRemote(Exception exception){

    }
}
