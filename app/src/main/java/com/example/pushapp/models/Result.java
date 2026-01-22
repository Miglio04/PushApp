package com.example.pushapp.models;
import java.util.List;

public abstract class Result {
    private Result() {}

    public boolean isTrainingsSuccess() {
        return this instanceof TrainingsSuccess;
    }
    public boolean isUserSuccess(){return this instanceof UserSuccess; }
    public boolean isSessionSuccess(){return this instanceof SessionSuccess; }


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
        private final String userId;
        public SessionSuccess(String userId) {
            this.userId = userId;
        }
        public String getData() {
            return userId;
        }
    }

    /**
     * Class that represents an error occurred during the interaction
     * with a Web Service or a local database.
     */
    public static final class Error extends Result {
        private final String message;
        public Error(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
