package com.example.pushapp.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.utils.Constants;
import com.example.pushapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ExerciseRepository implements ExerciseCallback {

    private ExerciseLocalDataSource exerciseLocalDataSource;
    private ExerciseAPIDataSource exerciseAPIDataSource;
    private MutableLiveData<Result> exercises;

    private SessionManager sessionManager;

    ExerciseRepository(ExerciseLocalDataSource exerciseLocalDataSource, ExerciseAPIDataSource exerciseAPIDataSource, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        exercises = new MutableLiveData<>();
        this.exerciseLocalDataSource = exerciseLocalDataSource;
        this.exerciseAPIDataSource = exerciseAPIDataSource;
        exerciseLocalDataSource.setCallback(this);
        exerciseAPIDataSource.setCallback(this);
    }



    public MutableLiveData<Result> getExercises(){
        return exercises;
    }
    public void fetchExercises(){
        long lastFetchTime = sessionManager.getLastApiFetchTime();
        long timeSinceLastFetch = System.currentTimeMillis() - lastFetchTime;

        if(lastFetchTime == 0 || timeSinceLastFetch > Constants.API_FETCH_INTERVAL){
            Log.e("Exercise Repository", "Sto fetchando dalle API");
            exerciseAPIDataSource.fetchAllExercises();
            lastFetchTime = System.currentTimeMillis();
            sessionManager.saveApiFetchTime(lastFetchTime);
        } else {
            Log.e("Exercise Repository", "Sto fetchando dal DB locale");
            exerciseLocalDataSource.getExercises();
        }
    }

    public void resetLocalDatabase(){
        exerciseLocalDataSource.deleteExercises();
        if(exercises != null){
            exercises.postValue(null);
            sessionManager.clearApiFetchTime();
        }
    }

    @Override
    public void onSuccessFromRemote(ArrayList<Exercise> exerciseList) {
        exercises.postValue(new Result.ExerciseSuccess(exerciseList));
        exerciseLocalDataSource.insertExercises(exerciseList);
    }
    @Override
    public void onSuccessFromLocalGet(List<Exercise> exerciseList) {
        exercises.postValue(new Result.ExerciseSuccess(exerciseList));
    }
    @Override
    public void onSuccessFromLocalDelete() {
        exercises.postValue(new Result.ExerciseSuccess(new ArrayList<>()));
    }

    @Override
    public void onFailureFromRemote(Exception e) {
        exerciseLocalDataSource.getExercises();
    }
    @Override
    public void onFailureFromLocal(Exception e) {
        exercises.postValue(new Result.Error(e));
    }
}
