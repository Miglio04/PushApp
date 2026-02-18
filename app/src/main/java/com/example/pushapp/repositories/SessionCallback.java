package com.example.pushapp.repositories;

import com.example.pushapp.models.SessionUser;
import com.example.pushapp.models.User;

/**
 * Callback interface for handling Session repository operations.
 * Manages callbacks for authentication events such as login, registration,
 * and password recovery outcomes.
 */
public interface SessionCallback {
    void onSuccessFromLogin(SessionUser sessionUser);
    void onFailureFromLogin(Exception e);
    void onSuccessFromRegister(SessionUser sessionUser);
    void onFailureFromRegister(Exception e);
    void onSuccessFromPasswordReset(String email);
    void onFailureFromPasswordReset(Exception e);
    void onUserNotFoundFromPasswordReset(Exception e);
}
