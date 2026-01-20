package com.example.pushapp.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ExerciseRepository {

    private ExerciseLocalDataSource exerciseLocalDataSource;
    private ExerciseAPIDataSource exerciseAPIDataSource;
    private MutableLiveData<Result> exercises;

    ExerciseRepository(ExerciseLocalDataSource exerciseLocalDataSource, ExerciseAPIDataSource exerciseAPIDataSource) {
        exercises = new MutableLiveData<>();
        this.exerciseLocalDataSource = exerciseLocalDataSource;
        this.exerciseAPIDataSource = exerciseAPIDataSource;
        // TO BE COMPLETED
    }

    public MutableLiveData<Result> getExercises(){
        return exercises;
    }
    public void fetchExercises(){
        //ottenere gli esercizi dal database locale
        //richiedere gli esercizi all'API (ogni tot)
        //aggiornare i liveData
    }



}