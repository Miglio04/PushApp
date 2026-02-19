package com.example.pushapp.utils;

/**
 * Application-wide constants for Firestore collections, API endpoints, and configuration values.
 */
public class Constants {

    public static final boolean DEBUG_MODE = true;
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_TRAININGS = "trainings";
    public static final String COLLECTION_ROUTINES = "routines";
    public static final String COLLECTION_WORKOUT_EXERCISES = "workoutExercises";

    public static final String BASE_EXERCISES_API_URL = "https://api.api-ninjas.com/";
    public static final String NINJA_API_KEY = "GbwJ1ZlJJQxuPTIf8Hnr5Q==g0AjKf0qK6MD3GpX";

    public static final long API_FETCH_INTERVAL = 7 * 24 * 60 * 60 * 1000;


}
