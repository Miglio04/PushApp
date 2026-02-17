package com.example.pushapp.models;

import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import java.util.List;

/**
 * Wrapper class for handling operation results and status.
 * Used to communicate success or failure of repository operations to ViewModels.
 */
public abstract class Result {
    private Result() {}

    /**
     * Metrics available for charting statistics.
     */
    public enum ChartMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }

    public boolean isTrainingsSuccess() { return this instanceof TrainingsSuccess; }
    public boolean isUserSuccess(){ return this instanceof UserSuccess; }
    public boolean isSessionSuccess(){ return this instanceof SessionSuccess; }
    public boolean isExerciseSuccess() { return this instanceof ExerciseSuccess; }

    public boolean isHistorySuccess() { return this instanceof HistorySuccess; }
    public boolean isGraphSuccess() { return this instanceof GraphSuccess; }

    public boolean isLocalDatabaseError(){ return this instanceof Error.LocalDatabaseError; }
    public boolean isRegistrationError(){ return this instanceof Error.RegistrationError; }
    public boolean isUserNotFound(){ return this instanceof Error.UserNotFound; }
    public boolean isForgotPasswordError(){ return this instanceof Error.ForgotPasswordError; }
    public boolean isForgotPasswordSuccess() { return this instanceof PasswordResetSuccess; }
    public boolean isLoginError(){ return this instanceof Error.LoginError; }
    public boolean isExerciseError() { return this instanceof Error.ExerciseError; }

    /**
     * Success result containing a list of Trainings.
     */
    public static final class TrainingsSuccess extends Result {
        private final List<Training> trainingList;
        public TrainingsSuccess(List<Training> trainingList) { this.trainingList = trainingList; }
        public List<Training> getData() { return trainingList; }
    }

    /**
     * Success result containing a User object.
     */
    public static final class UserSuccess extends Result {
        private final User user;
        public UserSuccess(User user) { this.user = user; }
        public User getData() { return user; }
    }

    /**
     * Success result containing a SessionUser object.
     */
    public static final class SessionSuccess extends Result{
        private final SessionUser sessionUser;
        public SessionSuccess(SessionUser sessionUser) { this.sessionUser= sessionUser; }
        public SessionUser getData() { return sessionUser; }
    }

    /**
     * Success result containing a list of history sessions.
     */
    public static final class HistorySuccess extends Result {
        private final List<HistorySessionWithExercises> historyList;

        public HistorySuccess(List<HistorySessionWithExercises> historyList) {
            this.historyList = historyList;
        }

        public List<HistorySessionWithExercises> getData() {
            return historyList;
        }
    }

    /**
     * Success result containing graph data points.
     */
    public static final class GraphSuccess extends Result {
        private final List<GraphPoint> points;
        private ChartMetric metric;

        public GraphSuccess(List<GraphPoint> points) {
            this.points = points;
        }

        public List<GraphPoint> getData() {
            return points;
        }

        public ChartMetric getMetric() {
            return metric;
        }

        public void setMetric(ChartMetric metric) {
            this.metric = metric;
        }
    }

    /**
     * Success result for password reset operations.
     */
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
     * Success result containing a list of Exercises.
     */
    public static final class ExerciseSuccess extends Result {
        private final List<Exercise> exerciseList;
        public ExerciseSuccess(List<Exercise> exerciseList) { this.exerciseList = exerciseList; }
        public List<Exercise> getData() { return exerciseList; }
    }

    /**
     * Error result class with specific error types.
     */
    public static class Error extends Result {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public Error(Exception e) {
            this.message = e.getMessage() != null ? e.getMessage() : "Errore sconosciuto";
        }

        public String getMessage() { return message; }

        /**
         * Error type for local database issues.
         */
        public static final class LocalDatabaseError extends Error {
            public LocalDatabaseError(String message) { super(message); }
        }
        /**
         * Error type for registration issues.
         */
        public static final class RegistrationError extends Error{
            public RegistrationError(String message) { super(message); }
        }

        /**
         * Error type for user not found scenarios.
         */
        public static final class UserNotFound extends Error {
            public UserNotFound(String message) {
                super(message);
            }
        }

        /**
         * Error type for forgot password scenarios.
         */
        public static final class ForgotPasswordError extends Error {
            public ForgotPasswordError(String message) {
                super(message);
            }
        }

        /**
         * Error type for login issues.
         */
        public static final class LoginError extends Error {
            public LoginError(String message) {
                super(message);
            }
        }

        /**
         * Error type for exercise related issues.
         */
        public static final class ExerciseError extends Error {
            public ExerciseError(String message) { super(message); }
        }
    }
}