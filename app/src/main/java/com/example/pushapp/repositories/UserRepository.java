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

    public MutableLiveData<Result> getCurrentUser() {
        return currentUser;
    }
    public void fetchUserById(String userId){
        localDataSource.getUserById(userId);
    }
    public void insertUser(User user){
        localDataSource.insertUser(user);
    }
    public void updateUser(User user){
        localDataSource.updateCurrentUser(user);
    }

    public void onSuccessFromLocalInsert(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        remoteDataSource.insertUser(user);
    }
    public void onSuccessFromLocalGet(User user, String userId){
        if(user == null){
            remoteDataSource.fetchUserById(userId);
        }else {
            currentUser.postValue(new Result.UserSuccess(user));
        }
    }
    public void onSuccessFromLocalUpdate(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        remoteDataSource.insertUser(user);
    }
    public void onSuccessFromRemoteFetch(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        localDataSource.insertUser(user);
    }


    public void onFailureFromLocal(Exception exception){
        currentUser.postValue(new Result.Error.LocalDatabaseError(exception.getMessage()));
    }
    public void onFailureFromRemote(Exception exception){
        currentUser.postValue(new Result.Error(exception.getMessage()));
    }

    public void clearLiveData(){
        currentUser.setValue(null);
    }
    public void resetLocalDatabase(){
        localDataSource.resetDatabase();
    }

}
