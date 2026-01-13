package com.example.pushapp.repositories;

import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.database.UserDao;
import com.example.pushapp.models.User;

public class UserLocalDataSource {
    private final UserDao userDao;
    private UserCallback userCallback = null;

    public UserLocalDataSource(LocalDatabase localDatabase) {
        this.userDao = localDatabase.userDao();
    }

    public void setUserCallback(UserCallback userCallback){
        this.userCallback = userCallback;
    }

    public void getUserById(String id){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getByUserId(id);
            if (user != null) {
                userCallback.onSuccessFromLocal(user);
            }else{
                userCallback.onFailureFromLocal(new Exception("User is null"));
            }
        });
    }

    public void updateCurrentUser(User user) {
    }

    public void insertNewCurrentUser(User user){}
}
