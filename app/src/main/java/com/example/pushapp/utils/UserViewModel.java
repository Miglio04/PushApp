package com.example.pushapp.utils;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {
    private static final String TAG = "UserViewModel";
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.d(TAG, "No user logged in");
            return;
        }

        isLoading.setValue(true);
        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                        Log.d(TAG, "User data loaded successfully");
                    } else {
                        Log.d(TAG, "User document does not exist");
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    isLoading.setValue(false);
                });
    }

    public void setUser(User user) {
        userLiveData.setValue(user);
    }
}
