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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

    public void createTraining(String userId, Training training) {
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("Local Training ID (UUID) is missing."));
            return;
        }

        String trainingId = training.getTrainingId();
        if (trainingId == null || trainingId.isEmpty()) {
            trainingCallback.onFailureFromRemote(new Exception("Local Training ID (UUID) is missing."));
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
        batch.commit();
    }

    // Valutare se dividere in più metodi
    public void fetchTrainings(String userId) {
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("User not authenticated for fetch."));
            return;
        }

        CollectionReference trainingsCollection = db.collection("users").document(userId).collection("trainings");

        // 1. Recupera i documenti Training principali dalla sottocollezione
        trainingsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(trainingSnapshots -> {
                    if (trainingSnapshots.isEmpty()) {
                        Log.d(TAG, "Nessun training trovato per l'utente, restituisco lista vuota.");
                        trainingCallback.onSuccessFromRemote(new ArrayList<>());
                        return;
                    }

                    List<Training> trainings = new ArrayList<>();
                    List<Task<Void>> allFetchTasks = new ArrayList<>();

                    for (DocumentSnapshot trainingDoc : trainingSnapshots.getDocuments()) {
                        Training training = trainingDoc.toObject(Training.class);
                        if (training != null) {
                            training.setTrainingId(trainingDoc.getId());
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
                                trainingCallback.onSuccessFromRemote(trainings);
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
                    routine.setRoutineId(routineDoc.getId());
                    routine.setTrainingId(training.getTrainingId());
                    routines.add(routine);
                    Task<Void> fetchExercisesTask = fetchExercisesForRoutine(routine, routineDoc.getReference());
                    exerciseTasks.add(fetchExercisesTask);
                }
            }
            training.setRoutinesList(new ArrayList<>(routines));
            return Tasks.whenAllComplete(exerciseTasks).continueWith(task -> null);
        });
    }

    private Task<Void> fetchExercisesForRoutine(Routine routine, DocumentReference routineRef) {
        return routineRef.collection("workoutExercises").get().onSuccessTask(exerciseSnapshots -> {
            List<WorkoutExercise> exercises = new ArrayList<>();

            for (DocumentSnapshot exerciseDoc : exerciseSnapshots.getDocuments()) {
                WorkoutExercise exercise = exerciseDoc.toObject(WorkoutExercise.class);
                if (exercise != null) {
                    exercise.setWorkoutExerciseId(exerciseDoc.getId());
                    exercise.setRoutineId(routine.getRoutineId());
                    if (exercise.getSeries() != null) {
                        for(Serie serie : exercise.getSeries()) {
                            serie.setWorkoutExerciseId(exercise.getWorkoutExerciseId());
                            serie.setUserId(auth.getUid());
                        }
                    }
                    exercises.add(exercise);
                }
            }
            routine.setWorkoutExercises(exercises);
            return Tasks.forResult(null);
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

            db.collection("users").document(auth.getCurrentUser().getUid())
                    .collection("trainings").document(training.getTrainingId())
                    .set(training)
                    .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
        }
    }

    public void deleteTraining(Training training) {
        if (training == null || training.getTrainingId() == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training or Training ID is null"));
            return;
        }

        String userId = training.getUserId();
        if (userId == null) {
            return;
        }

        DocumentReference trainingRef = db.collection("users").document(userId)
                .collection("trainings").document(training.getTrainingId());

        trainingRef.collection("routines").get().addOnSuccessListener(routineSnapshots -> {
            WriteBatch batch = db.batch();

            batch.delete(trainingRef);

            List<Task<QuerySnapshot>> exerciseTasks = new ArrayList<>();

            for (DocumentSnapshot routineDoc : routineSnapshots) {
                batch.delete(routineDoc.getReference());

                exerciseTasks.add(routineDoc.getReference().collection("workoutExercises").get());
            }

            if (exerciseTasks.isEmpty()) {
                batch.commit().addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
            } else {
                Tasks.whenAllSuccess(exerciseTasks).addOnSuccessListener(objects -> {
                    for (Object obj : objects) {
                        QuerySnapshot exerciseSnapshot = (QuerySnapshot) obj;
                        for (DocumentSnapshot exerciseDoc : exerciseSnapshot.getDocuments()) {
                            batch.delete(exerciseDoc.getReference());
                        }
                    }

                    batch.commit()
                            .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));

                }).addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
            }

        }).addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
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
