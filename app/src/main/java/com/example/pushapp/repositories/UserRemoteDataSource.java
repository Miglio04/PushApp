package com.example.pushapp.repositories;

import com.example.pushapp.models.User;
import com.example.pushapp.models.firebaseModels.FirebaseUser;
import com.example.pushapp.utils.converters.UserConverter;
import com.example.pushapp.utils.Constants;

import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Data source for handling user-related operations with the remote Firestore database.
 * Manages fetching and inserting user data in the cloud.
 */
public class UserRemoteDataSource {
    private final FirebaseFirestore db;
    private UserCallback userCallback = null;

    UserRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param userCallback The callback implementation.
     */
    public void setUserCallback(UserCallback userCallback) {
        this.userCallback = userCallback;
    }

    /**
     * Fetches user details from Firestore by their unique user ID.
     * Converts the remote FirebaseUser object to a local User domain object upon success.
     *
     * @param uId The unique ID of the user to fetch.
     */
    public void fetchUserById(String uId) {
        db.collection(Constants.COLLECTION_USERS).document(uId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseUser firebaseUser = documentSnapshot.toObject(FirebaseUser.class);
                        if (firebaseUser != null) {
                            User user = UserConverter.firebaseUserToUser(firebaseUser);
                            userCallback.onSuccessFromRemoteFetch(user);
                        }
                    }
                    else{
                        userCallback.onFailureFromRemote(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> userCallback.onFailureFromRemote(new Exception("Error fetching user")));
    }

    /**
     * Inserts or updates user data in Firestore.
     * Converts the local User domain object to a FirebaseUser DTO before saving.
     *
     * @param user The user object to insert.
     */
    public void insertUser(User user) {
        if (user == null || user.getUserId() == null) {
            userCallback.onFailureFromRemote(new Exception("null user or userId"));
        }
        else{
            FirebaseUser firebaseUser = UserConverter.userToFirebaseUser(user);

            db.collection(Constants.COLLECTION_USERS)
                    .document(user.getUserId())
                    .set(firebaseUser)
                    .addOnFailureListener(e -> {
                        if (userCallback != null) {
                            userCallback.onFailureFromRemote(e);
                        }
                    });
        }
    }
}
