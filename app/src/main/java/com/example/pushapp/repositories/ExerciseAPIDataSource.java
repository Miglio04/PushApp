package com.example.pushapp.repositories;

import androidx.annotation.NonNull;

import com.example.pushapp.api.ApiClient;
import com.example.pushapp.api.NinjaApiService;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.api.ExerciseApiModel;
import com.example.pushapp.utils.Constants;
import com.example.pushapp.utils.converters.ExerciseConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseAPIDataSource {
    private ExerciseCallback callback = null;
    NinjaApiService apiService;
    private final List<String> muscles = Arrays.asList(
            "abdominals", "abductors", "adductors", "biceps", "calves",
            "chest", "forearms", "glutes", "hamstrings", "lats",
            "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps"
    );

    ExerciseAPIDataSource() {
        apiService = ApiClient.getClient().create(NinjaApiService.class);
    }
    public void setCallback(ExerciseCallback callback){
        this.callback = callback;
    }

    public void fetchAllExercises() {

        List<ExerciseApiModel> allDownloadedExercises = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = muscles.size();

        for (String muscle : muscles) {
            Call<List<ExerciseApiModel>> call = apiService.getExercises(Constants.NINJA_API_KEY, muscle);

            call.enqueue(new Callback<List<ExerciseApiModel>>() {
                @Override
                public void onResponse(@NonNull Call<List<ExerciseApiModel>> call, @NonNull Response<List<ExerciseApiModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ExerciseApiModel> results = response.body();

                        if (!results.isEmpty()) {
                            allDownloadedExercises.addAll(results);
                        }
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(@NonNull Call<List<ExerciseApiModel>> call, @NonNull Throwable t) {
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (allDownloadedExercises.isEmpty()) {
                            callback.onFailureFromRemote(new Exception("No exercises found from API"));
                        } else {
                            // Sorting exercises alphabetically by name, ignoring case
                            Collections.sort(allDownloadedExercises, (e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
                            ArrayList<Exercise> exerciseList = new ArrayList<>();
                            for (ExerciseApiModel apiModel : allDownloadedExercises) {
                                Exercise exercise = ExerciseConverter.apiToExercise(apiModel);
                                exerciseList.add(exercise);
                            }
                            callback.onSuccessFromRemote(exerciseList);
                        }
                    }
                }
            });
        }
    }
}
