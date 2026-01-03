package com.example.pushapp.api;

import com.example.pushapp.models.ExerciseApiModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NinjaApiService {
    @GET("v1/exercises")
    Call<List<ExerciseApiModel>> getExercises(
            @Header("X-Api-Key") String apiKey,
            @Query("muscle") String muscle
    );
}
