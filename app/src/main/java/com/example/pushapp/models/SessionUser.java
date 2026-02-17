package com.example.pushapp.models;

/**
 * Lightweight class representing an authenticated user in the current session.
 * Used for session management without loading full user profile data.
 */
public class SessionUser {
    private String userId;
    private String email;

    /**
     * Constructs a new SessionUser.
     *
     * @param userId The unique user ID.
     * @param email  The user's email address.
     */
    public SessionUser(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
