package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import com.example.pushapp.models.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrainingRemoteDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private TrainingCallback trainingCallback;

    public TrainingRemoteDataSource(){
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.trainingCallback = null;
    }

    public void setTrainingCallback(TrainingCallback trainingCallback) {
        this.trainingCallback = trainingCallback;
    }

    public void updateTraining(Training training){
        if(training == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training is null"));
        } else if (training.getTrainingId() == null) {
                trainingCallback.onFailureFromRemote(new Exception("Training ID is null"));
        } else{
            training.setUpdatedAt(System.currentTimeMillis());

            db.collection(COLLECTION_TRAININGS)
                    .document(training.getTrainingId())
                    .set(training)
                    .addOnFailureListener(trainingCallback::onFailureFromRemote);
        }
    }
}
