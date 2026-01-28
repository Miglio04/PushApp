package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;

import android.util.Log;

import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.WorkoutExercise;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    private static final String TAG = "TrainingRemoteDS";

    TrainingRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.trainingCallback = null;
    }

    public void fetchTrainings() {
        newFetchTrainings();
    }

    public void oldFetchTrainings() {
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

    // Valutare se dividere in più metodi
    public void newFetchTrainings() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("User not authenticated for fetch."));
            return;
        }

        // 1. Recupera i documenti Training principali
        db.collection(COLLECTION_TRAININGS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(trainingSnapshots -> {
                    if (trainingSnapshots.isEmpty()) {
                        Log.d(TAG, "Nessun training trovato per l'utente, restituisco lista vuota.");
                        trainingCallback.onSuccessFromRemote(new ArrayList<>()); // Nessun training, operazione riuscita
                        return;
                    }

                    List<Training> trainings = new ArrayList<>();
                    List<Task<Void>> allFetchTasks = new ArrayList<>();

                    for (DocumentSnapshot trainingDoc : trainingSnapshots.getDocuments()) {
                        Training training = trainingDoc.toObject(Training.class);
                        if (training != null) {
                            training.setTrainingId(trainingDoc.getId()); // Imposta l'ID del documento
                            trainings.add(training);

                            // 2. Per ogni training, crea un Task per recuperare le sue routine
                            Task<Void> fetchRoutinesTask = fetchRoutinesForTraining(training, trainingDoc.getReference());
                            allFetchTasks.add(fetchRoutinesTask);
                        }
                    }

                    // 3. Attendi il completamento di TUTTI i task di fetch (routine, esercizi, serie)
                    Tasks.whenAllComplete(allFetchTasks)
                            .addOnSuccessListener(taskSnapshots -> {
                                Log.d(TAG, "Tutti i dati annidati sono stati recuperati con successo.");
                                trainingCallback.onSuccessFromRemote(trainings); // Ora 'trainings' è completamente "idratato"
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Errore nel recuperare i dati annidati.", e);
                                trainingCallback.onFailureFromRemote(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Errore nel recuperare i training principali.", e);
                    trainingCallback.onFailureFromRemote(e);
                });
    }

    private Task<Void> fetchRoutinesForTraining(Training training, DocumentReference trainingRef) {
        return trainingRef.collection("routines").get().onSuccessTask(routineSnapshots -> {
            List<Routine> routines = new ArrayList<>();
            List<Task<Void>> exerciseTasks = new ArrayList<>();

            for (DocumentSnapshot routineDoc : routineSnapshots.getDocuments()) {
                Routine routine = routineDoc.toObject(Routine.class);
                if (routine != null) {
                    routines.add(routine);
                    // 4. Per ogni routine, crea un Task per recuperare i suoi esercizi
                    Task<Void> fetchExercisesTask = fetchExercisesForRoutine(routine, routineDoc.getReference());
                    exerciseTasks.add(fetchExercisesTask);
                }
            }
            training.setRoutinesList(new ArrayList<>(routines));
            return Tasks.whenAllComplete(exerciseTasks).continueWith(task -> null); // Continua la catena
        });
    }

    private Task<Void> fetchExercisesForRoutine(Routine routine, DocumentReference routineRef) {
        return routineRef.collection("workoutExercises").get().onSuccessTask(exerciseSnapshots -> {
            List<WorkoutExercise> exercises = new ArrayList<>();
            List<Task<Void>> seriesTasks = new ArrayList<>();

            for (DocumentSnapshot exerciseDoc : exerciseSnapshots.getDocuments()) {
                WorkoutExercise exercise = exerciseDoc.toObject(WorkoutExercise.class);
                if (exercise != null) {
                    exercises.add(exercise);
                    // 5. Per ogni esercizio, crea un Task per recuperare le sue serie
                    Task<Void> fetchSeriesTask = fetchSeriesForExercise(exercise, exerciseDoc.getReference());
                    seriesTasks.add(fetchSeriesTask);
                }
            }
            routine.setWorkoutExercises(exercises);
            return Tasks.whenAllComplete(seriesTasks).continueWith(task -> null);
        });
    }

    private Task<Void> fetchSeriesForExercise(WorkoutExercise exercise, DocumentReference exerciseRef) {
        return exerciseRef.collection("series").get().onSuccessTask(seriesSnapshots -> {
            List<Serie> series = new ArrayList<>();
            for (DocumentSnapshot seriesDoc : seriesSnapshots.getDocuments()) {
                Serie serie = seriesDoc.toObject(Serie.class);
                if (serie != null) {
                    series.add(serie);
                }
            }
            exercise.setSeries(series);
            return Tasks.forResult(null); // Fine della catena per questo ramo
        });
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
