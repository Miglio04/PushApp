package com.example.pushapp.repositories;

import android.util.Log;

import com.example.pushapp.models.User;
import com.example.pushapp.models.firebaseModels.FirebaseUser;
import com.example.pushapp.utils.converters.UserConverter;
import com.example.pushapp.utils.Constants;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserRemoteDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private UserCallback userCallback = null;

    public UserRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void setUserCallback(UserCallback userCallback) {
        this.userCallback = userCallback;
    }

    public void fetchCurrentUser(){
        String userId = auth.getCurrentUser().getUid();
    }

    public void fetchUserById(String uId) {
        db.collection(Constants.COLLECTION_USERS).document(uId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseUser firebaseUser = documentSnapshot.toObject(FirebaseUser.class);
                        if (firebaseUser != null) {
                            User user = UserConverter.firebaseUserToUser(firebaseUser);
                            userCallback.onSuccessFromRemote(user);
                        }
                    }
                    else{
                        userCallback.onFailureFromRemote(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    userCallback.onFailureFromRemote(new Exception("Error fetching user"));
                });
    }

    public void insertUser(User user) {

        if (user == null || user.getUserId() == null) {
            Log.e("UserRemoteDataSource", "insertUser: User o UserId nullo, operazione annullata");
            userCallback.onFailureFromRemote(new Exception("null user or userId"));
        }
        else{
            Log.d("UserRemoteDataSource", "insertUser: Inizio conversione e inserimento per UID: " + user.getUserId());

            FirebaseUser firebaseUser = UserConverter.userToFirebaseUser(user);

            db.collection(Constants.COLLECTION_USERS)
                    .document(user.getUserId())
                    .set(firebaseUser)
                    .addOnSuccessListener(aVoid -> {
                        Log.i("UserRemoteDataSource", "insertUser: Successo! Utente salvato correttamente su Firestore.");
                        if (userCallback != null) {
                            // Se vuoi notificare il successo al repository/viewmodel
                            // userCallback.onSuccessFromRemote(user);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserRemoteDataSource", "insertUser: Errore durante l'inserimento dell'utente: " + e.getMessage(), e);
                        if (userCallback != null) {
                            userCallback.onFailureFromRemote(e);
                        }
                    });
        }
    }
}
