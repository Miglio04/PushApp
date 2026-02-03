package com.example.pushapp.models;
import java.util.List;

public abstract class Result {
    private Result() {}

    public boolean isTrainingsSuccess() {
        return this instanceof TrainingsSuccess;
    }
    public boolean isUserSuccess(){ return this instanceof UserSuccess; }
    public boolean isSessionSuccess(){ return this instanceof SessionSuccess; }
    public boolean isLocalDatabaseError(){ return this instanceof Error.LocalDatabaseError; }
    public boolean isRegistrationError(){ return this instanceof Error.RegistrationError; }
    public boolean isUserNotFound(){ return this instanceof Error.UserNotFound; }
    public boolean isForgotPasswordError(){ return this instanceof Error.ForgotPasswordError; }
    public boolean isForgotPasswordSuccess() { return this instanceof PasswordResetSuccess; }


    /**
     * Class that represents a successful action during the interaction
     * with a Web Service or a local database.
     */
    public static final class TrainingsSuccess extends Result {
        private final List<Training> trainingList;
        public TrainingsSuccess(List<Training> trainingList) {
            this.trainingList = trainingList;
        }
        public List<Training> getData() {
            return trainingList;
        }
    }

    public static final class UserSuccess extends Result {
        private final User user;
        public UserSuccess(User user) {
            this.user = user;
        }
        public User getData() {
            return user;
        }
    }

    public static final class SessionSuccess extends Result{
        private final SessionUser sessionUser;
        public SessionSuccess(SessionUser sessionUser) {
            this.sessionUser= sessionUser;
        }
        public SessionUser getData() { return sessionUser; }
    }

    public static final class PasswordResetSuccess extends Result {
        private final String email;
        public PasswordResetSuccess(String email) {
            this.email = email;
        }
        public String getEmail() {
            return email;
        }
    }

    /**
     * Class that represents an error occurred during the interaction
     * with a Web Service or a local database.
     */
    public static class Error extends Result {
        private final String message;
        public Error(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }

        public static final class LocalDatabaseError extends Error {
            public LocalDatabaseError(String message) {
                super(message);
            }
        }
        public static final class RegistrationError extends Error{
            public RegistrationError(String message) {
                super(message);
            }
        }

        public static final class UserNotFound extends Error {
            public UserNotFound(String message) {
                super(message);
            }
        }

        public static final class ForgotPasswordError extends Error {
            public ForgotPasswordError(String message) {
                super(message);
            }
        }
    }
}
