package com.example.pushapp.repositories;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.pushapp.api.ApiClient;
import com.example.pushapp.api.NinjaApiService;
import com.example.pushapp.models.ExerciseApiModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseRepository {

    private final FirebaseFirestore db;
    private final CollectionReference exercisesRef;

    // API KEY
    private static final String API_KEY = "GbwJ1ZlJJQxuPTIf8Hnr5Q==g0AjKf0qK6MD3GpX";

    private final List<String> muscles = Arrays.asList(
            "abdominals", "abductors", "adductors", "biceps", "calves",
            "chest", "forearms", "glutes", "hamstrings", "lats",
            "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps"
    );

    public ExerciseRepository() {
        db = FirebaseFirestore.getInstance();
        // V4: Nuova versione per forzare il riscaricamento con il mapping corretto
        exercisesRef = db.collection("available_exercises");
    }

    public void getAvailableExercises(final FirebaseCallback<List<ExerciseApiModel>> callback) {
        exercisesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot result = task.getResult();

                if (result != null && !result.isEmpty()) {
                    Log.d("REPO", "Cache V4 trovata: " + result.size() + " esercizi.");
                    List<ExerciseApiModel> exercises = new ArrayList<>();
                    for (QueryDocumentSnapshot document : result) {
                        exercises.add(document.toObject(ExerciseApiModel.class));
                    }
                    Collections.sort(exercises, (e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
                    callback.onSuccess(exercises);
                } else {
                    Log.d("REPO", "Cache V4 vuota. Avvio scaricamento API...");
                    fetchAllMusclesFromApi(callback);
                }
            } else {
                callback.onError(task.getException());
            }
        });
    }

    private void fetchAllMusclesFromApi(final FirebaseCallback<List<ExerciseApiModel>> callback) {
        NinjaApiService apiService = ApiClient.getClient().create(NinjaApiService.class);

        List<ExerciseApiModel> allDownloadedExercises = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = muscles.size();

        for (String muscle : muscles) {
            Call<List<ExerciseApiModel>> call = apiService.getExercises(API_KEY, muscle);

            call.enqueue(new Callback<List<ExerciseApiModel>>() {
                @Override
                public void onResponse(@NonNull Call<List<ExerciseApiModel>> call, @NonNull Response<List<ExerciseApiModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ExerciseApiModel> results = response.body();
                        Log.d("REPO", "Muscle: " + muscle + " | Items: " + results.size());

                        // DEBUG: Stampa il primo elemento per vedere se equipment c'è
                        if(!results.isEmpty()) {
                            Log.d("REPO", "Check Equipment: " + results.get(0).getName() + " -> " + results.get(0).getEquipment());
                        }

                        if (!results.isEmpty()) {
                            // Pulizia Dati
                            for(ExerciseApiModel ex : results) {
                                // Se l'equipment è nullo, proviamo a dedurlo o impostarlo a body_only se è type cardio/stretching
                                if (ex.getEquipment() == null || ex.getEquipment().isEmpty()) {
                                    if("body_only".equals(ex.getEquipment())) continue; // already set
                                    // Fallback opzionale, altrimenti lascia null ma gson dovrebbe averlo preso ora
                                }
                            }
                            allDownloadedExercises.addAll(results);
                            saveBatchToFirebase(results);
                        }
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(@NonNull Call<List<ExerciseApiModel>> call, @NonNull Throwable t) {
                    Log.e("REPO", "Fail: " + muscle + " -> " + t.getMessage());
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (allDownloadedExercises.isEmpty()) {
                            callback.onError(new Exception("Nessun esercizio."));
                        } else {
                            Collections.sort(allDownloadedExercises, (e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
                            callback.onSuccess(new ArrayList<>(allDownloadedExercises));
                        }
                    }
                }
            });
        }
    }

    private void saveBatchToFirebase(List<ExerciseApiModel> exercises) {
        for (ExerciseApiModel ex : exercises) {
            if (ex.getName() != null) {
                String safeId = ex.getName().replaceAll("/", "-").replaceAll("\\.", "");
                exercisesRef.document(safeId).set(ex);
            }
        }
    }
}