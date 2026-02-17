package com.example.pushapp.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.repositories.SessionRepository;
import com.example.pushapp.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {
    private static final String TAG = "UserViewModel";
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final LiveData<Result> userLiveData;
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>(false);
    private LiveData<Result> sessionLiveData;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public UserViewModel(UserRepository userRepository, SessionRepository sessionRepository){
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.sessionLiveData = sessionRepository.getSessionLiveData();
        this.userLiveData = userRepository.getCurrentUser();
    }

    public LiveData<Result> getUserLiveData() {
        return userLiveData;
    }
    public LiveData<Result> getSessionLiveData() {
        return sessionLiveData;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }

    public void fetchSessionUser(){
        sessionRepository.getSessionUser();
    }
    // loads user data after login
    public void fetchUser() {
        isLoading.setValue(true);

        sessionRepository.getSessionUser();
        Result result = sessionLiveData.getValue();
        if(result instanceof Result.SessionSuccess){
            userRepository.fetchUserById(((Result.SessionSuccess) result).getData().getUserId());
        }

        isLoading.setValue(false);
    }

    public void registerWithGoogle(String idToken){
        sessionRepository.signInWithGoogle(idToken);
        registrationObserveSessionLiveData();
    }

    public void signInWithGoogle(String idToken){
        sessionRepository.signInWithGoogle(idToken);
    }

    public void signInWithEmailAndPassword(String email, String password) {
        sessionRepository.signInWithEmailAndPassword(email, password);
    }

    public void registerWithEmailAndPassword(String email, String password) {
        sessionRepository.registerWithEmailAndPassword(email, password);
        registrationObserveSessionLiveData();
    }

    public void updateCurrentUser(User user){
        userRepository.updateUser(user);
    }

    private void registrationObserveSessionLiveData() {
        androidx.lifecycle.Observer<Result> sessionObserver = new androidx.lifecycle.Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if(result == null) return;
                Log.d(TAG, "Session LiveData changed: " + result.getClass().getSimpleName());
                if (result instanceof Result.SessionSuccess) {
                    String userId = ((Result.SessionSuccess) result).getData().getUserId();
                    String email = ((Result.SessionSuccess) result).getData().getEmail();
                    User user = new User(userId, email);

                    Log.d(TAG, "Inserting user in local database");
                    userRepository.insertUser(user);
                }
                sessionLiveData.removeObserver(this);
            }
        };
        sessionLiveData.observeForever(sessionObserver);
    }
    public void clearSessionLiveData() {
        sessionRepository.clearLiveData();
    }

    public void clearUserLiveData() {
        userRepository.clearLiveData();
    }

    public void clearLiveData() {
        sessionRepository.clearLiveData();
        userRepository.clearLiveData();
    }
    public void sendPasswordResetEmail(String email) {
        sessionRepository.sendPasswordResetEmail(email);
    }

    public void logout() {
        sessionRepository.logout();
    }

    public void resetLocalDatabase(){
        userRepository.resetLocalDatabase();
        clearLiveData();
    }
}
