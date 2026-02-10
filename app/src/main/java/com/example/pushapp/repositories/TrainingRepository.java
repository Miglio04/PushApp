package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.utils.TrainingListGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

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

    // attributo temporaneo; da rimuovere quando si implementa versioning.
    private boolean isFirstFetchCompleted = false;

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

    public LiveData<Result> getTrainingList(){
        trainingLocalDataSource.getTrainings();
        trainingRemoteDataSource.fetchTrainings();
        return trainingList;
    }

    public void createSampleTraining(FirebaseCallback<String> callback){
        createTraining(TrainingListGenerator.generateTrainingList(), callback);
    }

    // metodo da modificare (aggiorna direttamente Firestore)
    // viene utilizzato per generare sample trainings e lavora a cascata
    // il create dovrà creare solo il documento training senza routine/esercizi/serie
    public void createTraining(Training training, FirebaseCallback<String> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String trainingId = training.getTrainingId();
        if (trainingId == null || trainingId.isEmpty()) {
            callback.onError(new Exception("Local Training ID (UUID) is missing."));
            return;
        }

        // 1. Prepara un batch di scrittura atomica
        WriteBatch batch = db.batch();

        // 2. Prepara il documento per il Training principale
        DocumentReference trainingRef = db.collection("users").document(userId)
                .collection(COLLECTION_TRAININGS).document(trainingId);

        training.setUserId(userId);
        training.setCreatedAt(System.currentTimeMillis());
        training.setUpdatedAt(System.currentTimeMillis());
        training.setDeleted(false);

        // Aggiungi l'operazione di scrittura del Training al batch.
        batch.set(trainingRef, training);

        // 3. Itera sulle routine per salvarle nella loro collezione
        if (training.getRoutinesList() != null) {
            for (Routine routine : training.getRoutinesList()) {
                DocumentReference routineRef = trainingRef.collection("routines").document(routine.getRoutineId());
                routine.setTrainingId(trainingId);
                routine.setUserId(userId);
                batch.set(routineRef, routine);

                // 4. Itera sugli esercizi per salvarli nella loro collezione
                if (routine.getWorkoutExercises() != null) {
                    for (WorkoutExercise exercise : routine.getWorkoutExercises()) {
                        DocumentReference exerciseRef = routineRef.collection("workoutExercises").document(exercise.getWorkoutExerciseId());
                        exercise.setRoutineId(routine.getRoutineId());
                        exercise.setUserId(userId);
                        if (exercise.getSeries() != null) {
                            for (Serie serie : exercise.getSeries()) {
                                serie.setWorkoutExerciseId(exercise.getWorkoutExerciseId());
                                serie.setUserId(userId);
                            }
                        }
                        batch.set(exerciseRef, exercise);
                    }
                }
            }
        }

        // 5. Esegui tutte le operazioni nel batch
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(trainingId))
                .addOnFailureListener(callback::onError);
    }

    public void createTraining(Training training) {
        trainingLocalDataSource.createTraining(training);
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
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<Training>(trainingListSuccess));
        trainingList.postValue(result);
    }

    public void onFailureFromLocal(Exception exception) {
        Result.Error resultError = new Result.Error(exception.getMessage());
        trainingList.postValue(resultError);
    }

    // metodo in versione temporanea: non considera il versioning
    public void onSuccessFromRemote(List<Training> trainingListSuccess) {
        if(!isFirstFetchCompleted){
            trainingLocalDataSource.overwriteTrainings(trainingListSuccess, getCurrentUserId());
            // Finché dati sample salvati direttamente in firestore lasciare commentato
            // isFirstFetchCompleted = true;
        }
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
