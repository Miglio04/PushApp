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

    public void fetchExercisesByMuscle(String muscle, FirebaseCallback<List<ExerciseApiModel>> callback) {
        apiService.getExercises(API_KEY, muscle).enqueue(new Callback<List<ExerciseApiModel>>() {
            @Override
            public void onResponse(Call<List<ExerciseApiModel>> call, Response<List<ExerciseApiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("API Error: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<ExerciseApiModel>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getAllExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
        List<ExerciseApiModel> allExercises = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completedRequests = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (String muscle : muscles) {
            fetchExercisesByMuscle(muscle, new FirebaseCallback<List<ExerciseApiModel>>() {
                @Override
                public void onSuccess(List<ExerciseApiModel> result) {
                    allExercises.addAll(result);
                    checkCompletion();
                }

                @Override
                public void onError(Exception e) {
                    errorCount.incrementAndGet();
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (completedRequests.incrementAndGet() == muscles.size()) {
                        if (allExercises.isEmpty() && errorCount.get() > 0) {
                            callback.onError(new Exception("Failed to fetch exercises for all muscle groups"));
                        } else {
                            callback.onSuccess(new ArrayList<>(allExercises));
                        }
                    }
                }
            });
        }
    }

    public void getAvailableExercises(FirebaseCallback<List<ExerciseApiModel>> callback) {
        getAllExercises(callback);
    }
}
