package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.example.pushapp.utils.TrainingListGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class TrainingRepository implements TrainingCallback{
    private final String TAG = "TrainingRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private ListenerRegistration trainingsListener;
    private final TrainingLocalDataSource trainingLocalDataSource;

    private final TrainingRemoteDataSource trainingRemoteDataSource;
    private final MutableLiveData<Result> trainingList;
    TrainingRepository(TrainingLocalDataSource trainingLocalDataSource, TrainingRemoteDataSource trainingRemoteDataSource) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        trainingList = new MutableLiveData<>();
        this.trainingLocalDataSource = trainingLocalDataSource;
        this.trainingRemoteDataSource = trainingRemoteDataSource;
        trainingLocalDataSource.setTrainingCallback(this);
        trainingRemoteDataSource.setTrainingCallback(this);
    }

    // da spostare nella repository dell'utente
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void fetchTrainings(String userId){
        trainingLocalDataSource.fetchTrainings(userId);
    }

    public LiveData<Result> getTrainingList(){
        return trainingList;
    }

    public void createSampleTraining(String userId){
        createTraining(userId, TrainingListGenerator.generateTrainingList(userId));
        //createTraining(TrainingListGenerator.generateTrainingList(), callback);
    }

    public void createTraining(String userId, Training training) {
        trainingLocalDataSource.createTraining(userId, training);
    }

    public void detachTrainingsListener() {
        if (trainingsListener != null) {
            trainingsListener.remove();
            trainingsListener = null;
        }
    }

    public void getActiveTraining(FirebaseCallback<Training> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        db.collection(COLLECTION_TRAININGS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Training training = querySnapshot.getDocuments()
                                .get(0).toObject(Training.class);
                        callback.onSuccess(training);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateTraining(Training training){
        if(training != null){
            trainingLocalDataSource.updateTraining(training);
            trainingRemoteDataSource.updateTraining(training);
        }
    }

    // DELETE
    public void deleteTraining(Training training, FirebaseCallback<Void> callback) {

        trainingLocalDataSource.deleteTraining(training);

        /*db.collection(COLLECTION_TRAININGS)
                .document(trainingId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);*/
    }

    // SET ACTIVE
    public void setActiveTraining(String trainingId, FirebaseCallback<Void> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        // Disattiva tutti, poi attiva quello selezionato
        db.collection(COLLECTION_TRAININGS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().update("active", doc.getId().equals(trainingId));
                    }
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public void onSuccessFromLocal(List<Training> trainingListSuccess) {
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<Training>(trainingListSuccess));
        trainingList.postValue(result);
    }

    public void onFailureFromLocal(Exception exception) {
        Result.Error resultError = new Result.Error(exception.getMessage());
        trainingList.postValue(resultError);
    }

    public void onSuccessFromLocalFetch(String userId, List<Training> trainingListSuccess) {
        // Always update the UI with what we have locally
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<Training>(trainingListSuccess));
        trainingList.postValue(result);

        // Then check if we need to fetch from remote (e.g. empty list might mean not synced yet)
        if(trainingListSuccess.isEmpty()){
            trainingRemoteDataSource.fetchTrainings(userId);
        }
    }

    public void onSuccessFromLocalGet(List<Training> trainingListSuccess){
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<Training>(trainingListSuccess));
        trainingList.postValue(result);
    }

    public void onSuccessFromLocalCreate(String userId, Training training){
        trainingRemoteDataSource.createTraining(userId, training);
        trainingLocalDataSource.getTrainings();
    }

    public void onSuccessFromLocalDelete(Training training){
        if(training != null) {
            trainingRemoteDataSource.deleteTraining(training);
            trainingLocalDataSource.getTrainings();
        }
    }

    public void onSuccessFromRemote(List<Training> trainingListSuccess) {
        trainingLocalDataSource.overwriteTrainings(trainingListSuccess, getCurrentUserId());
    }
    public void onFailureFromRemote(Exception exception){
        // to implement: ritentare aggiornamento con workManager
    }

    public void resetLocalDatabase(){
        try{
            trainingLocalDataSource.resetDatabase();
        }catch (Exception e){
            Log.e(TAG, "resetLocalDatabase: " + e.getMessage());
        }
    }

}
