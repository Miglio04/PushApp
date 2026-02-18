package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.SessionUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Repository class responsible for managing user session and authentication operations.
 * Handles login, registration, password reset, and retrieving the current session
 * by coordinating between local storage and remote authentication services.
 */
public class SessionRepository implements SessionCallback {
    private final SessionLocalDataSource sessionLocalDataSource;
    private final SessionRemoteDataSource sessionRemoteDataSource;
    private final MutableLiveData<Result> sessionLiveData = new MutableLiveData<>();

    /**
     * Constructs a new SessionRepository.
     * Initializes data sources and sets this class as the callback for remote session events.
     *
     * @param sessionLocalDataSource  The local data source for session data.
     * @param sessionRemoteDataSource The remote data source for authentication.
     */
    SessionRepository(SessionLocalDataSource sessionLocalDataSource, SessionRemoteDataSource sessionRemoteDataSource) {
        this.sessionLocalDataSource = sessionLocalDataSource;
        this.sessionRemoteDataSource = sessionRemoteDataSource;
        sessionRemoteDataSource.setCallback(this);
    }

    /**
     * Returns the LiveData observing the result of session operations (login, register, etc.).
     *
     * @return MutableLiveData containing the operation Result.
     */
    public MutableLiveData<Result> getSessionLiveData() {
        return sessionLiveData;
    }

    /**
     * Initiates sign-in using Google credentials.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    public void signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        sessionRemoteDataSource.signInWithCredentials(credential);
    }

    /**
     * Attempts login only with Google credentials (does not register new users).
     * If user is not registered, triggers GoogleUserNotRegistered error.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    public void loginOnlyWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        sessionRemoteDataSource.loginOnlyWithCredentials(credential);
    }

    /**
     * Initiates sign-in using email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    public void signInWithEmailAndPassword(String email, String password) {
        sessionRemoteDataSource.signInWithEmailAndPassword(email, password);
    }

    /**
     * Initiates registration using email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    public void registerWithEmailAndPassword(String email, String password) {
        sessionRemoteDataSource.registerWithEmailAndPassword(email, password);
    }

    /**
     * Retrieves the current session user from local storage.
     * Updates the session LiveData with the result.
     */
    public void getSessionUser() {
        SessionUser sessionUser = sessionLocalDataSource.getCurrentSessionUser();
        if (sessionUser != null) {
            sessionLiveData.postValue(new Result.SessionSuccess(sessionUser));
        } else {
            sessionLiveData.postValue(new Result.Error.UserNotFound("User not found"));
        }
    }

    /**
     * Sends a password reset email to the specified address.
     *
     * @param email The email address to send the reset link to.
     */
    public void sendPasswordResetEmail(String email) {
        sessionRemoteDataSource.sendPasswordResetEmail(email);
    }

    /**
     * Logs out the current user from the remote session.
     */
    public void logout() {
        sessionRemoteDataSource.logout();
    }

    /**
     * Callback received upon successful login.
     * Updates LiveData with the session user.
     *
     * @param sessionUser The authenticated session user.
     */
    @Override
    public void onSuccessFromLogin(SessionUser sessionUser) {
        sessionLiveData.postValue(new Result.SessionSuccess(sessionUser));
    }

    /**
     * Callback received upon failed login.
     * Updates LiveData with the error.
     *
     * @param e The exception causing the failure.
     */
    @Override
    public void onFailureFromLogin(Exception e) {
        sessionLiveData.postValue(new Result.Error.LoginError(e.getMessage()));
    }

    /**
     * Callback received upon successful registration.
     * Updates LiveData with the new session user.
     *
     * @param sessionUser The registered session user.
     */
    public void onSuccessFromRegister(SessionUser sessionUser) {
        sessionLiveData.setValue(new Result.SessionSuccess(sessionUser));
    }

    /**
     * Callback received upon failed registration.
     * Updates LiveData with the error.
     *
     * @param e The exception causing the failure.
     */
    public void onFailureFromRegister(Exception e) {
        sessionLiveData.setValue(new Result.Error.RegistrationError(e.getMessage()));
    }

    /**
     * Callback received upon successful password reset email request.
     * Updates LiveData with success status.
     *
     * @param email The email address the reset link was sent to.
     */
    public void onSuccessFromPasswordReset(String email) {
        sessionLiveData.setValue(new Result.PasswordResetSuccess(email));
    }

    /**
     * Callback received upon failed password reset request.
     * Updates LiveData with the error.
     *
     * @param e The exception causing the failure.
     */
    public void onFailureFromPasswordReset(Exception e) {
        sessionLiveData.setValue(new Result.Error.ForgotPasswordError(e.getMessage()));
    }

    /**
     * Callback received when password reset fails because the user was not found.
     * Updates LiveData with a UserNotFound error.
     *
     * @param e The exception causing the failure.
     */
    public void onUserNotFoundFromPasswordReset(Exception e) {
        sessionLiveData.setValue(new Result.Error.UserNotFound(e.getMessage()));
    }

    /**
     * Callback received when Google login attempt finds user is not registered.
     * Updates LiveData with GoogleUserNotRegistered error.
     *
     * @param sessionUser The session user data from Google.
     */
    @Override
    public void onGoogleUserNotRegistered(SessionUser sessionUser) {
        sessionLiveData.setValue(new Result.Error.GoogleUserNotRegistered("User not registered", sessionUser));
    }

    /**
     * Clears the session LiveData value.
     */
    public void clearLiveData() {
        sessionLiveData.setValue(null);
    }

}
