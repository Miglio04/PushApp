package com.example.pushapp.api;

import com.example.pushapp.utils.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton client for handling Retrofit network requests.
 * Provides a configured Retrofit instance to interact with the external Exercise API.
 */
public class ApiClient {
    private static final String BASE_URL = Constants.BASE_EXERCISES_API_URL;
    private static Retrofit retrofit = null;

    /**
     * Retrieves the singleton Retrofit client instance.
     * Initializes the client with the base URL and Gson converter if not already created.
     *
     * @return The Retrofit instance.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}