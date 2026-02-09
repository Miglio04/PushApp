package com.example.pushapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.pushapp.models.Routine;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "workout_prefs";
    private static final String KEY_ROUTINE = "current_routine_json";
    private static final String KEY_START_TIME = "workout_start_time";
    private static final String KEY_IS_ACTIVE = "is_workout_active";

    private final SharedPreferences prefs;
    private final Gson gson; // Serve per trasformare l'oggetto in testo

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // Salva tutto lo stato attuale
    public void saveSession(Routine routine, long startTime) {
        String json = gson.toJson(routine);
        prefs.edit()
                .putString(KEY_ROUTINE, json)
                .putLong(KEY_START_TIME, startTime)
                .putBoolean(KEY_IS_ACTIVE, true)
                .apply(); // Salva in background
    }

    // Carica la routine salvata (se esiste)
    public Routine getRestoredRoutine() {
        String json = prefs.getString(KEY_ROUTINE, null);
        return json != null ? gson.fromJson(json, Routine.class) : null;
    }

    public long getStartTime() {
        return prefs.getLong(KEY_START_TIME, 0);
    }

    public boolean isSessionActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }

    // Pulisce tutto (da chiamare quando premi "Finish")
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}