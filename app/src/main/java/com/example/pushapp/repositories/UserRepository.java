package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;

/**
 * Repository class responsible for managing user data operations.
 * It coordinates interaction between the local database and remote data source
 * to fetch, insert, and update user information.
 */
public class UserRepository implements UserCallback {
    private final UserLocalDataSource localDataSource;
    private final UserRemoteDataSource remoteDataSource;
    private final MutableLiveData<Result> currentUser;

    /**
     * Constructs a new UserRepository.
     * Initializes data sources and sets up callbacks.
     *
     * @param localDataSource   The local data source for user data.
     * @param remoteDataSource  The remote data source for user data.
     */
    UserRepository(UserLocalDataSource localDataSource, UserRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setUserCallback(this);
        this.remoteDataSource.setUserCallback(this);
        this.currentUser = new MutableLiveData<>();
    }

    /**
     * Returns the LiveData observing the current user operation result.
     * @return MutableLiveData containing the Result (Success or Error).
     */
    public MutableLiveData<Result> getCurrentUser() {
        return currentUser;
    }

    /**
     * Initiates a user fetch operation by ID from the local data source.
     * If not found locally, it may trigger a remote fetch via callback.
     *
     * @param userId The ID of the user to fetch.
     */
    public void fetchUserById(String userId){
        localDataSource.getUserById(userId);
    }

    /**
     * Inserts a user into the local data source.
     *
     * @param user The user object to insert.
     */
    public void insertUser(User user){
        localDataSource.insertUser(user);
    }

    /**
     * Updates an existing user in the local data source.
     *
     * @param user The user object with updated information.
     */
    public void updateUser(User user){
        localDataSource.updateCurrentUser(user);
    }

    /**
     * Callback invoked when a user is successfully inserted into the local database.
     * Posts the success result and initiates synchronization with the remote data source.
     *
     * @param user The user that was inserted.
     */
    public void onSuccessFromLocalInsert(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        remoteDataSource.insertUser(user);
    }

    /**
     * Callback invoked when a user is retrieved from the local database.
     * If the user is missing locally, triggers a fetch from the remote data source.
     *
     * @param user   The user retrieved locally (may be null).
     * @param userId The ID of the user requested.
     */
    public void onSuccessFromLocalGet(User user, String userId){
        if(user == null){
            remoteDataSource.fetchUserById(userId);
        }else {
            currentUser.postValue(new Result.UserSuccess(user));
        }
    }

    /**
     * Callback invoked when a user is successfully updated in the local database.
     * Posts the success result and initiates synchronization with the remote data source.
     *
     * @param user The user that was updated.
     */
    public void onSuccessFromLocalUpdate(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        remoteDataSource.insertUser(user);
    }

    /**
     * Callback invoked when a user is successfully fetched from the remote data source.
     * Posts the success result and updates the local database with the fresh data.
     *
     * @param user The user fetched from remote.
     */
    public void onSuccessFromRemoteFetch(User user){
        currentUser.postValue(new Result.UserSuccess(user));
        localDataSource.insertUser(user);
    }


    /**
     * Callback invoked when a local database operation fails.
     * Posts a LocalDatabaseError result.
     *
     * @param exception The exception returned by the local data source.
     */
    public void onFailureFromLocal(Exception exception){
        currentUser.postValue(new Result.Error.LocalDatabaseError(exception.getMessage()));
    }

    /**
     * Callback invoked when a remote data source operation fails.
     * Posts a generic Error result.
     *
     * @param exception The exception returned by the remote data source.
     */
    public void onFailureFromRemote(Exception exception){
        currentUser.postValue(new Result.Error(exception.getMessage()));
    }

    /**
     * Clears the current user LiveData value.
     */
    public void clearLiveData(){
        currentUser.setValue(null);
    }

    /**
     * Resets the local database, clearing user data.
     */
    public void resetLocalDatabase(){
        localDataSource.resetDatabase();
    }

}
