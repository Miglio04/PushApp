package com.example.pushapp.repositories;

import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.database.UserDao;
import com.example.pushapp.models.User;

public class UserLocalDataSource {
    private final UserDao userDao;
    private UserCallback userCallback = null;

    UserLocalDataSource(LocalDatabase localDatabase) {
        this.userDao = localDatabase.userDao();
    }

    public void setUserCallback(UserCallback userCallback){
        this.userCallback = userCallback;
    }

    public void getUserById(String id){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getByUserId(id);
            userCallback.onSuccessFromLocalGet(user);
        });
    }

    public void updateCurrentUser(User user) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            if(user != null) {
                userDao.update(user);
                userCallback.onSuccessFromLocalUpdate(user);
            }
        });
    }

    public void insertNewCurrentUser(User user){

    }

    public void insertUser(User user) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            if(user != null) {
                userDao.insert(user);
                userCallback.onSuccessFromLocalInsert(user);
            }
        });
    }

    public void resetDatabase() {
        LocalDatabase.databaseWriteExecutor.execute(userDao::deleteAll);
    }

}
