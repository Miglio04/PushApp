package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.COLLECTION_ROUTINES;
import static com.example.pushapp.utils.Constants.COLLECTION_TRAININGS;
import static com.example.pushapp.utils.Constants.COLLECTION_USERS;
import static com.example.pushapp.utils.Constants.COLLECTION_WORKOUT_EXERCISES;

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

/**
 * Data source for handling training-related operations with the remote Firestore database.
 * Manages the creation, retrieval, update, and deletion of training plans, routines, and exercises in the cloud.
 */
public class TrainingRemoteDataSource {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private TrainingCallback trainingCallback;

    TrainingRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.trainingCallback = null;
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param trainingCallback The callback implementation.
     */
    public void setTrainingCallback(TrainingCallback trainingCallback) {
        this.trainingCallback = trainingCallback;
    }

    /**
     * Creates a new training plan in Firestore along with its routines and exercises.
     * Uses a batch write to ensure atomicity.
     *
     * @param userId   The ID of the user creating the training.
     * @param training The Training object to save remotely.
     */
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

        WriteBatch batch = db.batch();

        DocumentReference trainingRef = db.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_TRAININGS).document(trainingId);

        training.setUserId(userId);
        training.setCreatedAt(System.currentTimeMillis());

        batch.set(trainingRef, training);

        if (training.getRoutinesList() != null) {
            for (Routine routine : training.getRoutinesList()) {
                DocumentReference routineRef = trainingRef.collection(COLLECTION_ROUTINES).document(routine.getRoutineId());
                routine.setTrainingId(trainingId);
                routine.setUserId(userId);
                batch.set(routineRef, routine);

                if (routine.getWorkoutExercises() != null) {
                    for (WorkoutExercise exercise : routine.getWorkoutExercises()) {
                        DocumentReference exerciseRef = routineRef.collection(COLLECTION_WORKOUT_EXERCISES).document(exercise.getWorkoutExerciseId());
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

    /**
     * Fetches all training plans for a specific user from Firestore.
     * Retrieving is hierarchical: Trainings -> Routines -> Exercises.
     *
     * @param userId The ID of the user whose trainings are to be fetched.
     */
    public void fetchTrainings(String userId) {
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("User not authenticated for fetch."));
            return;
        }

        CollectionReference trainingsCollection = db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_TRAININGS);

        trainingsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(trainingSnapshots -> {
                    if (trainingSnapshots.isEmpty()) {
                        trainingCallback.onSuccessFromRemote(new ArrayList<>(), userId);
                        return;
                    }

                    List<Training> trainings = new ArrayList<>();
                    List<Task<Void>> allFetchTasks = new ArrayList<>();

                    for (DocumentSnapshot trainingDoc : trainingSnapshots.getDocuments()) {
                        Training training = trainingDoc.toObject(Training.class);
                        if (training != null) {
                            training.setTrainingId(trainingDoc.getId());
                            trainings.add(training);

                            Task<Void> fetchRoutinesTask = fetchRoutinesForTraining(training, trainingDoc.getReference());
                            allFetchTasks.add(fetchRoutinesTask);
                        }
                    }

                    Tasks.whenAllComplete(allFetchTasks)
                            .addOnSuccessListener(taskSnapshots -> {
                                trainingCallback.onSuccessFromRemote(trainings, userId);
                            })
                            .addOnFailureListener(e -> {
                                trainingCallback.onFailureFromRemote(e);
                            });
                })
                .addOnFailureListener(e -> {
                    trainingCallback.onFailureFromRemote(e);
                });
    }

    /**
     * Updates an existing training plan's top-level details in Firestore.
     *
     * @param training The Training object containing updated information.
     */
    public void updateTraining(Training training) {
        if (training == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training is null"));
        } else if (training.getTrainingId() == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training ID is null"));
        } else {
            db.collection(COLLECTION_USERS).document(training.getUserId())
                    .collection(COLLECTION_TRAININGS).document(training.getTrainingId())
                    .set(training)
                    .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
        }
    }

    /**
     * Deletes a training plan and all its sub-collections (Routines, WorkoutExercises) from Firestore.
     *
     * @param training The Training object to delete.
     */
    public void deleteTraining(Training training) {
        if (training == null || training.getTrainingId() == null) {
            trainingCallback.onFailureFromRemote(new Exception("Training or Training ID is null"));
            return;
        }

        String userId = training.getUserId();
        if (userId == null) {
            return;
        }

        DocumentReference trainingRef = db.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_TRAININGS).document(training.getTrainingId());

        trainingRef.collection(COLLECTION_ROUTINES).get().addOnSuccessListener(routineSnapshots -> {
            WriteBatch batch = db.batch();

            batch.delete(trainingRef);

            List<Task<QuerySnapshot>> exerciseTasks = new ArrayList<>();

            for (DocumentSnapshot routineDoc : routineSnapshots) {
                batch.delete(routineDoc.getReference());

                exerciseTasks.add(routineDoc.getReference().collection(COLLECTION_WORKOUT_EXERCISES).get());
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

    /**
     * ADDs a new routine to an existing training plan in Firestore.
     * Also saves any exercises contained within the routine.
     *
     * @param routine The Routine object to add.
     */
    public void createRoutine(Routine routine) {
        String userId = routine.getUserId();
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("User ID is missing."));
            return;
        }

        String trainingId = routine.getTrainingId();
        if (trainingId == null || trainingId.isEmpty()) {
            trainingCallback.onFailureFromRemote(new Exception("Training ID is missing."));
            return;
        }

        WriteBatch batch = db.batch();

        DocumentReference routineRef = db.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_TRAININGS).document(trainingId)
                .collection(COLLECTION_ROUTINES).document(routine.getRoutineId());

        batch.set(routineRef, routine);

        if (routine.getWorkoutExercises() != null) {
            for (WorkoutExercise exercise : routine.getWorkoutExercises()) {
                DocumentReference exerciseRef = routineRef.collection(COLLECTION_WORKOUT_EXERCISES)
                        .document(exercise.getWorkoutExerciseId());

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

        batch.commit().addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
    }

    /**
     * Updates an existing routine in Firestore.
     * Handles updating routine details and synchronizing the list of exercises (adding/removing).
     *
     * @param routine The Routine object with updated data.
     */
    public void updateRoutine(Routine routine){
        String userId = routine.getUserId();
        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("Local Training ID (UUID) is missing."));
            return;
        }

        String trainingId = routine.getTrainingId();
        if (trainingId == null || trainingId.isEmpty()) {
            trainingCallback.onFailureFromRemote(new Exception("Local Training ID (UUID) is missing."));
            return;
        }

        DocumentReference routineRef = db.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_TRAININGS).document(trainingId).collection(COLLECTION_ROUTINES).document(routine.getRoutineId());

        routineRef.collection(COLLECTION_WORKOUT_EXERCISES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    batch.set(routineRef, routine);

                    List<String> newExerciseIds = new ArrayList<>();
                    if (routine.getWorkoutExercises() != null) {
                        for (WorkoutExercise exercise : routine.getWorkoutExercises()) {
                            newExerciseIds.add(exercise.getWorkoutExerciseId());
                        }
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!newExerciseIds.contains(doc.getId())) {
                            batch.delete(doc.getReference());
                        }
                    }

                    if (routine.getWorkoutExercises() != null) {
                        for (WorkoutExercise exercise : routine.getWorkoutExercises()) {
                            DocumentReference exerciseRef = routineRef.collection(COLLECTION_WORKOUT_EXERCISES).document(exercise.getWorkoutExerciseId());
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

                    batch.commit()
                            .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));

                })
                .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
    }

    /**
     * Deletes a specific routine and its sub-collection of exercises from Firestore.
     *
     * @param routine The Routine object to delete.
     */
    public void deleteRoutine(Routine routine){
        String userId = routine.getUserId();
        String trainingId = routine.getTrainingId();
        String routineId = routine.getRoutineId();

        if (userId == null) {
            trainingCallback.onFailureFromRemote(new Exception("User not authenticated."));
            return;
        }

        DocumentReference routineRef = db.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_TRAININGS).document(trainingId)
                .collection(COLLECTION_ROUTINES).document(routineId);

        routineRef.collection(COLLECTION_WORKOUT_EXERCISES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    batch.delete(routineRef);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
                })
                .addOnFailureListener(e -> trainingCallback.onFailureFromRemote(e));
    }

    /**
     * Helper method to fetch all routines associated with a specific training document.
     *
     * @param training The parent training object being populated.
     * @param trainingRef The DocumentReference to the training in Firestore.
     * @return A Task that completes when all routines and their exercises are fetched.
     */
    private Task<Void> fetchRoutinesForTraining(Training training, DocumentReference trainingRef) {
        return trainingRef.collection(COLLECTION_ROUTINES).get().onSuccessTask(routineSnapshots -> {
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

    /**
     * Helper method to fetch all exercises associated with a specific routine document.
     *
     * @param routine The parent routine object being populated.
     * @param routineRef The DocumentReference to the routine in Firestore.
     * @return A Task that completes when the exercises are fetched and assigned to the routine.
     */
    private Task<Void> fetchExercisesForRoutine(Routine routine, DocumentReference routineRef) {
        return routineRef.collection(COLLECTION_WORKOUT_EXERCISES).get().onSuccessTask(exerciseSnapshots -> {
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

}
