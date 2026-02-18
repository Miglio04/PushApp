package com.example.pushapp.repositories;

import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.database.UserDao;
import com.example.pushapp.models.User;

/**
 * Data source for handling user-related operations with the local Room database.
 * Executes database operations asynchronously and communicates results via callbacks.
 */
public class UserLocalDataSource {
    private final UserDao userDao;
    private UserCallback userCallback = null;

    UserLocalDataSource(LocalDatabase localDatabase) {
        this.userDao = localDatabase.userDao();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param userCallback The callback implementation.
     */
    public void setUserCallback(UserCallback userCallback){
        this.userCallback = userCallback;
    }

    /**
     * Retrieves a user by their unique ID from the local database.
     * Executed on a background thread.
     *
     * @param id The unique ID of the user to fetch.
     */
    public void getUserById(String id){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getByUserId(id);
            if (userCallback != null) {
                userCallback.onSuccessFromLocalGet(user, id);
            }
        });
    }

    /**
     * Updates an existing user's information in the local database.
     * Executed on a background thread.
     *
     * @param user The user object with updated data.
     */
    public void updateCurrentUser(User user) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            if(user != null) {
                userDao.update(user);
                if (userCallback != null) {
                    userCallback.onSuccessFromLocalUpdate(user);
                }
            }
        });
    }

    /**
     * Inserts a new user into the local database.
     * Executed on a background thread.
     *
     * @param user The user object to insert.
     */
    public void insertUser(User user) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            if(user != null) {
                userDao.insert(user);
                if (userCallback != null) {
                    userCallback.onSuccessFromLocalInsert(user);
                }
            }
        });
    }

    /**
     * Clears all user data from the local database.
     * Executed on a background thread.
     */
    public void resetDatabase() {
        LocalDatabase.databaseWriteExecutor.execute(userDao::deleteAll);
    }

}
