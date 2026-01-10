package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class TrainingRepository implements TrainingCallback{
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private ListenerRegistration trainingsListener;
    private final TrainingLocalDataSource trainingLocalDataSource;

    private final TrainingRemoteDataSource trainingRemoteDataSource;
    private final MutableLiveData<Result> trainingList;

    // attributo temporaneo; da rimuovere quando si implementa versioning.
    private boolean isFirstFetchCompleted = false;

    public TrainingRepository(TrainingLocalDataSource trainingLocalDataSource, TrainingRemoteDataSource trainingRemoteDataSource) {
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

    public LiveData<Result> getTrainingList() {
        return newGetTrainingList();
    }

    public MutableLiveData<Result> oldGetTrainingList() {
        trainingLocalDataSource.getTrainings();
        return trainingList;
    }

    public MutableLiveData<Result> newGetTrainingList(){
        trainingLocalDataSource.getTrainings();
        trainingRemoteDataSource.fetchTrainings();
        return trainingList;
    }

    // CREATE
    // metodo da rimuovere (aggiorna direttamente Firestore)
    public void createTraining(Training training, FirebaseCallback<String> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        training.setUserId(userId);
        training.setCreatedAt(System.currentTimeMillis());
        training.setUpdatedAt(System.currentTimeMillis());

        DocumentReference docRef = db.collection(COLLECTION_TRAININGS).document();
        training.setTrainingId(docRef.getId());

        docRef.set(training)
                .addOnSuccessListener(aVoid -> callback.onSuccess(training.getTrainingId()))
                .addOnFailureListener(callback::onError);
    }

    public void createTraining(Training training) {
        trainingLocalDataSource.createTraining(training);
    }

    // READ - Singolo training
    public void getTraining(String trainingId, FirebaseCallback<Training> callback) {
        db.collection(COLLECTION_TRAININGS)
                .document(trainingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Training training = doc.toObject(Training.class);
                        callback.onSuccess(training);
                    } else {
                        callback.onError(new Exception("Training not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void attachUserTrainingsListener(FirebaseCallback<List<Training>> callback) {
        String userId = getCurrentUserId();
        Log.d("TrainingRepository", "attachUserTrainingsListener called for userId: " + userId);

        if (userId == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        // Se c'è già un listener attivo, lo rimuoviamo prima di crearne uno nuovo
        if (trainingsListener != null) {
            Log.d("TrainingRepository", "Removing existing listener");
            trainingsListener.remove();
        }

        Log.d("TrainingRepository", "Creating new snapshot listener...");

        trainingsListener = db.collection(COLLECTION_TRAININGS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    Log.d("TrainingRepository", "Snapshot listener triggered!");

                    if (error != null) {
                        Log.e("TrainingRepository", "Listener error: " + error.getMessage());
                        callback.onError(error);
                        return;
                    }

                    List<Training> trainings = new ArrayList<>();

                    if (querySnapshot != null) {
                        Log.d("TrainingRepository", "Snapshot received with " + querySnapshot.size() + " documents");
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Training t = doc.toObject(Training.class);
                            if (t != null) {
                                t.setTrainingId(doc.getId()); // Assicurati che l'ID sia impostato
                                Log.d("TrainingRepository", "  Doc ID: " + doc.getId());
                                trainings.add(t);
                            }
                        }
                    } else {
                        Log.d("TrainingRepository", "querySnapshot is null");
                    }

                    callback.onSuccess(trainings);

                });
        Log.d("TrainingRepository", "Listener attached successfully");

    }

    /**
     * Rimuove il listener quando non è più necessario (es. quando il ViewModel viene distrutto).
     */
    public void detachTrainingsListener() {
        if (trainingsListener != null) {
            trainingsListener.remove();
            trainingsListener = null;
        }
    }

    // READ - Training attivo
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

    // UPDATE
    // metodo da rimuovere (aggiorna direttamente Firestore)
    public void updateTraining(Training training, FirebaseCallback<Void> callback) {
        if (training.getTrainingId() == null) {
            callback.onError(new Exception("Training ID is null"));
            return;
        }

        training.setUpdatedAt(System.currentTimeMillis());

        db.collection(COLLECTION_TRAININGS)
                .document(training.getTrainingId())
                .set(training)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void updateTraining(Training training){
        if(training != null){
            trainingLocalDataSource.updateTraining(training);
            trainingRemoteDataSource.updateTraining(training);
        }
    }

    // DELETE
    public void deleteTraining(String trainingId, FirebaseCallback<Void> callback) {
        db.collection(COLLECTION_TRAININGS)
                .document(trainingId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
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
        Result.Success result = new Result.Success(new ArrayList<Training>(trainingListSuccess));
        trainingList.postValue(result);
    }

    public void onFailureFromLocal(Exception exception) {
        Result.Error resultError = new Result.Error(exception.getMessage());
        trainingList.postValue(resultError);
    }

    // metodo in versione temporanea: non considera il versioning
    public void onSuccessFromRemote(List<Training> trainingListSuccess) {
        if(!isFirstFetchCompleted){
            trainingLocalDataSource.overwriteTrainigs(trainingListSuccess, getCurrentUserId());
            isFirstFetchCompleted = true;
        }
    }
    public void onFailureFromRemote(Exception exception){
        // to implement: ritentare aggiornamento con workManager
    }
}
