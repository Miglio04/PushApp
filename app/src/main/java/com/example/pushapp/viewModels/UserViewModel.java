package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.repositories.SessionRepository;
import com.example.pushapp.repositories.UserRepository;

/**
 * ViewModel responsible for managing user authentication and user profile data.
 * Handles login, registration, password reset, and session management.
 */
public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final LiveData<Result> userLiveData;
    private final LiveData<Result> sessionLiveData;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /**
     * Constructor for UserViewModel.
     *
     * @param userRepository    Repository for user data operations.
     * @param sessionRepository Repository for session and authentication operations.
     */
    public UserViewModel(UserRepository userRepository, SessionRepository sessionRepository){
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.sessionLiveData = sessionRepository.getSessionLiveData();
        this.userLiveData = userRepository.getCurrentUser();
    }

    /**
     * Returns LiveData containing the current user's profile data result.
     */
    public LiveData<Result> getUserLiveData() {
        return userLiveData;
    }
    /**
     * Returns LiveData containing the current session result (e.g., authenticated user info).
     */
    public LiveData<Result> getSessionLiveData() {
        return sessionLiveData;
    }

    /**
     * Triggers fetching of the current session user from the repository.
     */
    public void fetchSessionUser(){
        sessionRepository.getSessionUser();
    }

    /**
     * Loads the authenticated user's profile data from the repository.
     * Gets current user from Firebase Auth and fetches their data.
     */
    public void fetchUser() {
        isLoading.setValue(true);

        com.google.firebase.auth.FirebaseUser firebaseUser =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            userRepository.fetchUserById(firebaseUser.getUid());
        }

        isLoading.setValue(false);
    }

    /**
     * Initiates Google registration using the provided ID token.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    public void registerWithGoogle(String idToken){
        sessionRepository.signInWithGoogle(idToken);
    }

    /**
     * Initiates Google Sign-In using the provided ID token.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    public void signInWithGoogle(String idToken){
        sessionRepository.signInWithGoogle(idToken);
    }

    /**
     * Attempts login only with Google (does not register new users).
     * If user is not registered, returns GoogleUserNotRegistered error.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    public void loginOnlyWithGoogle(String idToken) {
        sessionRepository.loginOnlyWithGoogle(idToken);
    }

    /**
     * Initiates sign-in with email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    public void signInWithEmailAndPassword(String email, String password) {
        sessionRepository.signInWithEmailAndPassword(email, password);
    }

    /**
     * Initiates registration with email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    public void registerWithEmailAndPassword(String email, String password) {
        sessionRepository.registerWithEmailAndPassword(email, password);
    }

    /**
     * Updates the current user's profile data.
     *
     * @param user The updated User object.
     */
    public void updateCurrentUser(User user){
        userRepository.updateUser(user);
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user The new User object to insert.
     */
    public void insertUser(User user){
        userRepository.insertUser(user);
    }


    /**
     * Clears the session LiveData in the repository.
     */
    public void clearSessionLiveData() {
        sessionRepository.clearLiveData();
    }

    /**
     * Clears the user LiveData in the repository.
     */
    public void clearUserLiveData() {
        userRepository.clearLiveData();
    }

    /**
     * Clears both session and user LiveData.
     */
    public void clearLiveData() {
        sessionRepository.clearLiveData();
        userRepository.clearLiveData();
    }

    /**
     * Sends a password reset email to the specified address.
     *
     * @param email The email address to send the reset link to.
     */
    public void sendPasswordResetEmail(String email) {
        sessionRepository.sendPasswordResetEmail(email);
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        sessionRepository.logout();
    }

    /**
     * Resets the local user database and clears ViewModel LiveData.
     */
    public void resetLocalDatabase(){
        userRepository.resetLocalDatabase();
        clearLiveData();
    }
}
