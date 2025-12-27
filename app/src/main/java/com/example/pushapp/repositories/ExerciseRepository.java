package com.example.pushapp.repositories;

import com.example.pushapp.models.api.ExerciseInfo;
import java.util.ArrayList;
import java.util.List;

// TODO: Implementare le chiamate di rete con Retrofit
public class ExerciseRepository {

    public ExerciseRepository() {
        // In futuro, qui inizializzerai Retrofit
    }

    /**
     * Metodo fittizio che simula il caricamento degli esercizi da un'API.
     * @param callback La callback per restituire il risultato.
     */
    public void getAvailableExercises(FirebaseCallback<List<ExerciseInfo>> callback) {
        // Simuliamo una risposta di successo dall'API
        try {
            List<ExerciseInfo> fakeApiResult = new ArrayList<>();
            fakeApiResult.add(new ExerciseInfo(1, "Bench Press"));
            fakeApiResult.add(new ExerciseInfo(2, "Squat"));
            fakeApiResult.add(new ExerciseInfo(3, "Deadlift"));
            fakeApiResult.add(new ExerciseInfo(8, "Pull-ups"));
            fakeApiResult.add(new ExerciseInfo(10, "Bicep Curls"));

            callback.onSuccess(fakeApiResult);

        } catch (Exception e) {
            callback.onError(e);
        }
    }
}

