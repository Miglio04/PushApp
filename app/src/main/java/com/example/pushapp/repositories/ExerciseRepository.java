package com.example.pushapp.repositories;

import com.example.pushapp.api.NinjaApiService;
import com.example.pushapp.models.ExerciseApiModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExerciseRepository {
    private static final String BASE_URL = "https://api.api-ninjas.com/";
    // NOTA: La chiave API è stata lasciata qui ma è una buona pratica gestirla in modo più sicuro.
    private static final String API_KEY = "GbwJ1ZlJJQxuPTIf8Hnr5Q==g0AjKf0qK6MD3GpX";
    private final NinjaApiService apiService;

    private final List<String> muscles = Arrays.asList(
            "abdominals", "abductors", "adductors", "biceps", "calves",
            "chest", "forearms", "glutes", "hamstrings", "lats",
            "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps"
    );

    public ExerciseRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(NinjaApiService.class);
    }

    /*
     ================================================================================
     == SEZIONE 1: LOGICA DI TEST (ATTIVA) CON DATI FITTIZI (MOCK)                 ==
     ================================================================================
     Questa sezione è attualmente in uso per evitare di consumare la quota API.
    */

    /**
     * [ATTIVO] Metodo che simula il recupero degli esercizi per un singolo muscolo.
     * Restituisce una lista di dati fittizi.
     * @param muscle Il gruppo muscolare per cui generare gli esercizi.
     * @param callback Il callback per restituire i risultati.
     */
    public void fetchExercisesByMuscle(String muscle, FirebaseCallback<List<ExerciseApiModel>> callback) {
        // Invece di una chiamata di rete, usiamo il nostro metodo per creare dati fittizi.
        List<ExerciseApiModel> mockData = createMockExercises(muscle);

        // Simula una risposta di successo immediata.
        callback.onSuccess(mockData);

        // Per testare un caso di errore, commenta la riga sopra e de-commenta la seguente:
        // callback.onError(new Exception("Errore simulato di rete per il muscolo: " + muscle));
    }

    /**
     * [ATTIVO] Recupera tutti gli esercizi per tutti i gruppi muscolari definiti.
     * Attualmente, usa la logica fittizia perché chiama la versione mock di `fetchExercisesByMuscle`.
     * @param callback Il callback per restituire la lista completa di esercizi.
     */
    public void getAllExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
        List<ExerciseApiModel> allExercises = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalMuscles = muscles.size();

        if (totalMuscles == 0) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        for (String muscle : muscles) {
            // Questa chiamata ora eseguirà la logica fittizia definita sopra
            fetchExercisesByMuscle(muscle, new FirebaseCallback<List<ExerciseApiModel>>() {
                @Override
                public void onSuccess(List<ExerciseApiModel> result) {
                    if (result != null) {
                        allExercises.addAll(result);
                    }
                    checkCompletion();
                }

                @Override
                public void onError(Exception e) {
                    // Anche se c'è un errore (simulato), continuiamo per non bloccare l'app
                    checkCompletion();
                }

                private void checkCompletion() {
                    // Controlla se tutte le "chiamate" (simulate) sono terminate
                    if (completedRequests.incrementAndGet() == totalMuscles) {
                        // Ordina alfabeticamente per nome per un risultato consistente
                        Collections.sort(allExercises, (e1, e2) -> e1.getName().compareTo(e2.getName()));
                        callback.onSuccess(new ArrayList<>(allExercises));
                    }
                }
            });
        }
    }

    /**
     * [ATTIVO] Fornisce gli esercizi disponibili. È un alias per getAllExercises.
     * @param callback Il callback da passare a getAllExercises.
     */
    public void getAvailableExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
        getAllExercises(callback);
    }


    /**
     * Metodo di supporto che crea una lista di esercizi fittizi per un dato gruppo muscolare.
     * @param muscle Il gruppo muscolare per cui generare gli esercizi.
     * @return Una lista di ExerciseApiModel fittizi.
     */
    private List<ExerciseApiModel> createMockExercises(String muscle) {
        List<ExerciseApiModel> mockList = new ArrayList<>();

        if ("biceps".equalsIgnoreCase(muscle)) {
            ExerciseApiModel bicepCurl = new ExerciseApiModel();
            bicepCurl.setName("Dumbbell Bicep Curl (Fittizio)");
            bicepCurl.setMuscle("biceps");
            bicepCurl.setDifficulty("beginner");
            bicepCurl.setEquipment("dumbbell");
            bicepCurl.setType("strength");
            bicepCurl.setInstructions("1. Stand holding dumbbells at your sides, palms facing forward. 2. Curl the weights up to your shoulders, keeping elbows stationary. 3. Squeeze biceps at the top, then slowly lower.");
            mockList.add(bicepCurl);

            ExerciseApiModel hammerCurl = new ExerciseApiModel();
            hammerCurl.setName("Hammer Curl (Fittizio)");
            hammerCurl.setMuscle("biceps");
            hammerCurl.setDifficulty("beginner");
            hammerCurl.setEquipment("dumbbell");
            hammerCurl.setType("strength");
            hammerCurl.setInstructions("1. Hold dumbbells with a neutral grip (palms facing each other). 2. Curl the weights up, maintaining the neutral grip. 3. Lower slowly and repeat.");
            mockList.add(hammerCurl);

        } else if ("chest".equalsIgnoreCase(muscle)) {
            ExerciseApiModel pushup = new ExerciseApiModel();
            pushup.setName("Push-up (Fittizio)");
            pushup.setMuscle("chest");
            pushup.setDifficulty("intermediate");
            pushup.setEquipment("body only");
            pushup.setType("strength");
            pushup.setInstructions("1. Start in a plank position. 2. Lower your body until your chest nearly touches the floor. 3. Push back up to the starting position.");
            mockList.add(pushup);

        } else if ("abdominals".equalsIgnoreCase(muscle)) {
            ExerciseApiModel crunch = new ExerciseApiModel();
            crunch.setName("Crunch (Fittizio)");
            crunch.setMuscle("abdominals");
            crunch.setDifficulty("beginner");
            crunch.setEquipment("body only");
            crunch.setType("strength");
            crunch.setInstructions("1. Lie on your back, knees bent. 2. Lift your upper body towards your knees. 3. Lower back down slowly.");
            mockList.add(crunch);
        }
        // Aggiungi qui altri "else if" per i gruppi muscolari che vuoi testare.

        return mockList;
    }


    /*
     ================================================================================
     == SEZIONE 2: LOGICA REALE (INATTIVA) CON VERE CHIAMATE API                   ==
     ================================================================================
     Quando avrai finito di testare, commenta l'intera SEZIONE 1 e de-commenta
     l'intera SEZIONE 2 per ripristinare il comportamento originale.
    */

//    /**
//     * [INATTIVO] Metodo ORIGINALE che effettua la vera chiamata API per un singolo muscolo.
//     * @param muscle Il gruppo muscolare da cercare.
//     * @param callback Il callback per restituire i risultati.
//     */
//    public void fetchExercisesByMuscle(String muscle, FirebaseCallback<List<ExerciseApiModel>> callback) {
//        apiService.getExercises(API_KEY, muscle).enqueue(new Callback<List<ExerciseApiModel>>() {
//            @Override
//            public void onResponse(Call<List<ExerciseApiModel>> call, Response<List<ExerciseApiModel>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError(new Exception("API Error: " + response.code()));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<ExerciseApiModel>> call, Throwable t) {
//                callback.onError(new Exception(t));
//            }
//        });
//    }
//
//    /**
//     * [INATTIVO] Metodo ORIGINALE che recupera tutti gli esercizi da tutti i gruppi muscolari.
//     * Contiene la logica per contare gli errori.
//     * @param callback Il callback per restituire la lista completa di esercizi.
//     */
//    public void getAllExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
//        List<ExerciseApiModel> allExercises = Collections.synchronizedList(new ArrayList<>());
//        AtomicInteger completedRequests = new AtomicInteger(0);
//        AtomicInteger errorCount = new AtomicInteger(0);
//
//        for (String muscle : muscles) {
//            fetchExercisesByMuscle(muscle, new FirebaseCallback<List<ExerciseApiModel>>() {
//                @Override
//                public void onSuccess(List<ExerciseApiModel> result) {
//                    allExercises.addAll(result);
//                    checkCompletion();
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    errorCount.incrementAndGet();
//                    checkCompletion();
//                }
//
//                private void checkCompletion() {
//                    if (completedRequests.incrementAndGet() == muscles.size()) {
//                        if (allExercises.isEmpty() && errorCount.get() > 0) {
//                            callback.onError(new Exception("Failed to fetch exercises for all muscle groups"));
//                        } else {
//                            callback.onSuccess(new ArrayList<>(allExercises));
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * [INATTIVO] Metodo ORIGINALE che fornisce gli esercizi disponibili.
//     * @param callback Il callback da passare a getAllExercises.
//     */
//    public void getAvailableExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
//        getAllExercises(callback);
//    }
}
