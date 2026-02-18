package com.example.pushapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "workout_session_prefs";
    private static final String KEY_SESSION_STATE = "routine_json";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_API_FETCH_TIME = "api_fetch_time";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    public void saveSessionState(WorkoutState state, long startTime) {
        SharedPreferences.Editor editor = prefs.edit();
        String stateJson = gson.toJson(state);
        editor.putString(KEY_SESSION_STATE, stateJson);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putBoolean(KEY_IS_ACTIVE, true);
        editor.apply();
    }

    public WorkoutState getSavedSession() {
        String json = prefs.getString(KEY_SESSION_STATE, null);
        if (json == null) return null;

        try {
            return gson.fromJson(json, WorkoutState.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getSavedStartTime() {
        return prefs.getLong(KEY_START_TIME, 0L);
    }

    public void saveApiFetchTime(long fetchTime) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_API_FETCH_TIME, fetchTime);
        editor.apply();
    }

    public long getLastApiFetchTime(){
        return prefs.getLong(KEY_API_FETCH_TIME, 0L);
    }

    public boolean isSessionActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_NAME);
        editor.remove(KEY_SESSION_STATE);
        editor.remove(KEY_START_TIME);
        editor.remove(KEY_IS_ACTIVE);
        editor.apply();
    }

    public void clearApiFetchTime() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_API_FETCH_TIME);
        editor.apply();
    }
}