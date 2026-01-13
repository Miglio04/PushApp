package com.example.pushapp.api;
import com.example.pushapp.models.api.food.EdamamResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EdamamApi {
    @GET("auto-complete")
    Call<List<String>> getSuggestions(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("q") String query
    );

    @GET("api/food-database/v2/parser")
    Call<EdamamResponse> getFoodDetails(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("ingr") String ingredient,
            @Query("nutrition-type") String type
    );
}