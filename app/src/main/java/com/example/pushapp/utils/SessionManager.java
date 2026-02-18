package com.example.pushapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Manages the persistence of workout session state and API fetch timestamps using SharedPreferences.
 * Allows saving and restoring active workout sessions across app restarts.
 */
public class SessionManager {

    private static final String PREF_NAME = "workout_session_prefs";
    private static final String KEY_SESSION_STATE = "routine_json";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_API_FETCH_TIME = "api_fetch_time";

    private final SharedPreferences prefs;
    private final Gson gson;

    /**
     * Constructs a new SessionManager.
     *
     * @param context The application context used to access SharedPreferences.
     */
    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Saves the current workout state and start time to SharedPreferences.
     * Marks the session as active.
     *
     * @param state     The WorkoutState object representing the current session.
     * @param startTime The start time of the workout in milliseconds.
     */
    public void saveSessionState(WorkoutState state, long startTime) {
        SharedPreferences.Editor editor = prefs.edit();
        String stateJson = gson.toJson(state);
        editor.putString(KEY_SESSION_STATE, stateJson);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putBoolean(KEY_IS_ACTIVE, true);
        editor.apply();
    }

    /**
     * Retrieves the saved workout state from SharedPreferences.
     *
     * @return The saved WorkoutState object, or null if no session is saved or deserialization fails.
     */
    public WorkoutState getSavedSession() {
        String json = prefs.getString(KEY_SESSION_STATE, null);
        if (json == null) return null;

        try {
            return gson.fromJson(json, WorkoutState.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves the start time of the saved session.
     *
     * @return The start time in milliseconds, or 0 if not found.
     */
    public long getSavedStartTime() {
        return prefs.getLong(KEY_START_TIME, 0L);
    }

    /**
     * Saves the timestamp of the last successful API fetch.
     *
     * @param fetchTime The timestamp in milliseconds.
     */
    public void saveApiFetchTime(long fetchTime) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_API_FETCH_TIME, fetchTime);
        editor.apply();
    }

    /**
     * Retrieves the timestamp of the last API fetch.
     *
     * @return The timestamp in milliseconds, or 0 if never fetched.
     */
    public long getLastApiFetchTime(){
        return prefs.getLong(KEY_API_FETCH_TIME, 0L);
    }

    /**
     * Checks if there is currently an active workout session saved.
     *
     * @return true if a session is active, false otherwise.
     */
    public boolean isSessionActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }

    /**
     * Clears all saved session data from SharedPreferences.
     * Effectively ends the current session persistence.
     */
    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_NAME);
        editor.remove(KEY_SESSION_STATE);
        editor.remove(KEY_START_TIME);
        editor.remove(KEY_IS_ACTIVE);
        editor.apply();
    }

    /**
     * Clears the stored API fetch timestamp.
     */
    public void clearApiFetchTime() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_API_FETCH_TIME);
        editor.apply();
    }
}