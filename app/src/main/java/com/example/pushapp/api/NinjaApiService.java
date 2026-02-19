package com.example.pushapp.api;

import com.example.pushapp.models.api.ExerciseApiModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit interface defining endpoints for the Ninja Exercises API.
 * Used to fetch exercise data from the external service.
 */
public interface NinjaApiService {

    /**
     * Fetches a list of exercises filtered by a specific muscle group.
     *
     * @param apiKey The API key for authentication.
     * @param muscle The target muscle group filter (e.g., "biceps", "chest").
     * @return A Retrofit Call object containing a list of ExerciseApiModel objects.
     */
    @GET("v1/exercises")
    Call<List<ExerciseApiModel>> getExercises(
            @Header("X-Api-Key") String apiKey,
            @Query("muscle") String muscle
    );
}