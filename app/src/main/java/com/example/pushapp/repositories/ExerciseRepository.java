package com.example.pushapp.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.api.ApiClient;
import com.example.pushapp.api.NinjaApiService;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.api.ExerciseApiModel;
import com.example.pushapp.repositories.dataSources.ExerciseAPIDataSource;
import com.example.pushapp.repositories.dataSources.ExerciseLocalDataSource;
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

    private ExerciseLocalDataSource exerciseLocalDataSource;
    private ExerciseAPIDataSource exerciseAPIDataSource;
    private MutableLiveData<Result> exercises;

    public MutableLiveData<Result> getExercises(){
        return exercises;
    }
    public void fetchExercises(){
        //ottenere gli esercizi dal database locale
        //richiedere gli esercizi all'API (ogni tot)
        //aggiornare i liveData
    }

    // da rimuovere: viene chiamato da TrainingviewModel, ma il viewModel dovrebbe chiamare fetchExercises
    // serviva a ottenere gli esercizi direttamente dall'API
    public void getAvailableExercises(FirebaseCallback<List<ExerciseApiModel>> callback){}

}