package com.example.pushapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "workout_session_prefs";
    private static final String KEY_SESSION_STATE = "routine_json";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_IS_ACTIVE = "is_active";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // --- SALVATAGGIO STATO ---
    public void saveSessionState(HistorySessionWithExercises session, long startTime) {
        SharedPreferences.Editor editor = prefs.edit();
        String routineJson = gson.toJson(session);
        editor.putString(KEY_SESSION_STATE, routineJson);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putBoolean(KEY_IS_ACTIVE, true);
        editor.apply();
    }

    // --- RECUPERO DATI ---
    public HistorySessionWithExercises getSavedSession() {
        String json = prefs.getString(KEY_SESSION_STATE, null);
        if (json == null) return null;

        try {
            return gson.fromJson(json, HistorySessionWithExercises.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getSavedStartTime() {
        return prefs.getLong(KEY_START_TIME, 0L);
    }

    public boolean isSessionActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}