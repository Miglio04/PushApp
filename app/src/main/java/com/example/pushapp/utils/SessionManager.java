package com.example.pushapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.pushapp.models.Routine;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "workout_session_prefs";
    private static final String KEY_ROUTINE = "routine_json";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_IS_ACTIVE = "is_active";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // --- SALVATAGGIO STATO ---
    /**
     * Salva l'intera routine e il tempo di inizio.
     * Viene chiamato ogni volta che l'utente modifica qualcosa (es. spunta un set).
     */
    public void saveSessionState(Routine routine, long startTime) {
        SharedPreferences.Editor editor = prefs.edit();

        // Convertiamo l'oggetto Routine in una stringa JSON
        String routineJson = gson.toJson(routine);

        editor.putString(KEY_ROUTINE, routineJson);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putBoolean(KEY_IS_ACTIVE, true); // "Allenamento in corso"

        editor.apply(); // Salvataggio asincrono (non blocca la UI)
    }

    // --- RECUPERO DATI ---
    /**
     * Recupera la Routine salvata (se esiste).
     * Include protezione contro errori di parsing (es. dopo aggiornamento app).
     */
    public Routine getSavedRoutine() {
        String json = prefs.getString(KEY_ROUTINE, null);
        if (json == null) return null;

        try {
            return gson.fromJson(json, Routine.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Se il JSON è corrotto o la classe è cambiata, restituiamo null
            // per evitare crash all'avvio.
            return null;
        }
    }

    /**
     * Recupera l'orario di inizio salvato.
     */
    public long getSavedStartTime() {
        return prefs.getLong(KEY_START_TIME, 0L);
    }

    /**
     * Controlla se c'è una sessione appesa (crash o chiusura forzata).
     */
    public boolean isSessionActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }

    // --- PULIZIA ---

    /**
     * Pulisce tutto. Da chiamare quando l'allenamento è FINITO e SALVATO nel DB.
     */
    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}