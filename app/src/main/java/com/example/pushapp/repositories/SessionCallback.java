package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.example.pushapp.models.User;

public interface SessionCallback {
    void onSuccessFromLogin(SessionUser sessionUser);
    void onFailureFromLogin(Exception e);
    void onSuccessFromRegister(SessionUser sessionUser);
    void onFailureFromRegister(Exception e);
    void onSuccessFromPasswordReset(String email);
    void onFailureFromPasswordReset(Exception e);
    void onUserNotFoundFromPasswordReset(Exception e);
}
