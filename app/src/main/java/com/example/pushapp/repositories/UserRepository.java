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

    //provvisorio per primo accesso fino a implementazione versioning
    private boolean firstFetchCompleted = false;

    public UserRepository(UserLocalDataSource localDataSource, UserRemoteDataSource remoteDataSource, SessionRepository sessionRepository) {
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
    public void fetchUserById(String userId){
        localDataSource.getUserById(userId);
    }

    public void insertUser(User user){
        localDataSource.insertUser(user);
        Log.d(TAG, "Inserting user in local database");
    }

    public void updateUser(User user){
        localDataSource.updateCurrentUser(user);
    }

    public void onSuccessFromLocalInsert(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        Log.d(TAG, "Local database operation successful");
        remoteDataSource.insertUser(user);
    }
    public void onFailureFromLocal(Exception exception){
        currentUser.postValue(new Result.Error.LocalDatabaseError(exception.getMessage()));
    }
    //temporary version of method: not considering versioning and user's local storage
    public void onSuccessFromRemote(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        localDataSource.insertUser(user);
    }
    //provvisorio: quando verrà implementato il workmanager dovrà ritentare la richiesta
    public void onFailureFromRemote(Exception exception){
        currentUser.postValue(new Result.Error(exception.getMessage()));
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

    public void clearLiveData(){
        currentUser.setValue(null);
    }

    public void resetLocalDatabase(){
        localDataSource.resetDatabase();
    }

}
