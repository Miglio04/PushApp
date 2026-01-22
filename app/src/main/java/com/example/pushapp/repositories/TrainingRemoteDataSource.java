package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import com.example.pushapp.models.Training;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrainingRemoteDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private TrainingCallback trainingCallback;

    TrainingRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.trainingCallback = null;
    }

    public void fetchTrainings() {
        // get trainings from Firestore
        Task<QuerySnapshot> query = db.collection(COLLECTION_TRAININGS)
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        // set listeners
        query.addOnSuccessListener(querySnapshot ->
            trainingCallback.onSuccessFromRemote(getTrainingsFromSnapshot(querySnapshot)));
        query.addOnFailureListener(e ->
                trainingCallback.onFailureFromRemote(e));
    }

    public void setTrainingCallback(TrainingCallback trainingCallback) {
        this.trainingCallback = trainingCallback;
    }

    public void updateTraining(Training training) {
        if (training == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training is null"));
        } else if (training.getTrainingId() == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training ID is null"));
        } else {
            training.setUpdatedAt(System.currentTimeMillis());

            db.collection(COLLECTION_TRAININGS)
                    .document(training.getTrainingId())
                    .set(training)
                    .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
        }
    }

    // used to parse the QuerySnapshot into a list of Training objects
    private List<Training> getTrainingsFromSnapshot(QuerySnapshot querySnapshot) {
        List<DocumentSnapshot> documents = querySnapshot.getDocuments();
        List<Training> trainings = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Training training = document.toObject(Training.class);
            if (training != null) {
                trainings.add(training);
            }
        }
        return trainings;
    }

}
