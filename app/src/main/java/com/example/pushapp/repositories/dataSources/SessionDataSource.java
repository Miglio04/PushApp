package com.example.pushapp.repositories.dataSources;

import com.example.pushapp.repositories.UserCallback;
import com.google.firebase.auth.FirebaseAuth;

public class SessionDataSource {
    private final FirebaseAuth auth;
    private UserCallback userCallback = null;

    public SessionDataSource() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void setUserCallback(UserCallback userCallback){
        this.userCallback = userCallback;
    }

    public String getCurrentUserId(){
        if(auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }else{
            return null;
        }
    }

}
