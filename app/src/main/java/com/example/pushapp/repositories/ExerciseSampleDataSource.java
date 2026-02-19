package com.example.pushapp.repositories;

import com.example.pushapp.models.Exercise;

import java.util.ArrayList;

/**
 * Data source di esempio usata solo per debug.
 *
 * <p>Fornisce una lista hard-coded di istanze {@link Exercise} e notifica il risultato
 * tramite un {@code ExerciseCallback} impostato con {@link #setCallback(ExerciseCallback)}.
 * Questo data source è sincrono e progettato esclusivamente per ambienti di sviluppo/debug.</p>
 *
 * @see Exercise
 * @see ExerciseCallback
 */
public class ExerciseSampleDataSource {

    /**
     * Callback usata per restituire i risultati.
     *
     * <p>Può essere {@code null} se non è stato impostato alcun listener.</p>
     */
    private ExerciseCallback callback = null;

    /**
     * Costruttore vuoto.
     */
    public ExerciseSampleDataSource() {}

    /**
     * Imposta il callback che verrà notificato quando i dati di esempio saranno pronti.
     *
     * @param callback istanza di {@code ExerciseCallback} o {@code null} per rimuovere il listener
     */
    public void setCallback(ExerciseCallback callback){
        this.callback = callback;
    }

    /**
     * Recupera le esercitazioni di esempio e, se presente, notifica il {@code callback}
     * invocando {@code onSuccessFromRemote} con la lista di esercizi.
     *
     * <p>La lista è generata localmente e non effettua alcuna chiamata di rete.</p>
     */
    public void getSampleExercises() {
        ArrayList<Exercise> sampleExercises = new ArrayList<>();
        sampleExercises.add(new Exercise("Exercise debug 1", "chest", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 2", "chest", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 3", "back", "beginner"));
        sampleExercises.add(new Exercise("Exercise debug 4", "back", "intermediate"));
        sampleExercises.add(new Exercise("Exercise debug 5", "biceps", "intermediate"));
        sampleExercises.add(new Exercise("Exercise debug 6", "biceps", "expert"));
        sampleExercises.add(new Exercise("Exercise debug 7", "triceps", "expert"));

        if(callback != null) {
            callback.onSuccessFromRemote(sampleExercises);
        }
    }
}
