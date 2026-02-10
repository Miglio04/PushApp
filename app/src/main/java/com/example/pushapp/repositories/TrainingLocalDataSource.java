package com.example.pushapp.repositories;

import android.util.Log;

import com.example.pushapp.database.WorkoutExerciseDao;
import com.example.pushapp.database.RoutineDao;
import com.example.pushapp.database.SerieDao;
import com.example.pushapp.database.TrainingDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.TrainingWithRoutines;
import com.example.pushapp.models.roomModels.helpers.WorkoutExerciseWithSeries;

import java.util.ArrayList;
import java.util.List;

public class TrainingLocalDataSource {
    private final String TAG = "TrainingLocalDataSource";
    private TrainingCallback trainingCallback;
    private final TrainingDao trainingDao;
    private final RoutineDao routineDao;
    private final WorkoutExerciseDao workoutExerciseDao;
    private final SerieDao serieDao;
    private final LocalDatabase localDatabase; // Field added for transactions

    TrainingLocalDataSource(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.trainingDao = localDatabase.trainingDao();
        this.routineDao = localDatabase.routineDao();
        this.workoutExerciseDao = localDatabase.workoutExerciseDao();
        this.serieDao = localDatabase.serieDao();
        this.trainingCallback = null;
    }

    public void setTrainingCallback(TrainingCallback trainingCallback){
        this.trainingCallback = trainingCallback;
    }

    public void getTrainings() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<TrainingWithRoutines> trainingsWithRoutines = trainingDao.getAllTrainingsWithRoutines();
                List<Training> finalTrainingList = new ArrayList<>();

                if (trainingsWithRoutines == null) {
                    trainingCallback.onFailureFromLocal(new Exception("Query result is null"));
                    return;
                }

                for (TrainingWithRoutines twr : trainingsWithRoutines) {
                    Training training = twr.training;
                    List<Routine> routines = twr.routines != null ? twr.routines : new ArrayList<>();
                    if (!routines.isEmpty()) {
                        for (Routine routine : routines) {
                            List<WorkoutExerciseWithSeries> exercisesWithSeries =
                                    workoutExerciseDao.getExercisesWithSeriesByRoutineId(routine.getRoutineId());
                            if (exercisesWithSeries != null && !exercisesWithSeries.isEmpty()) {
                                List<WorkoutExercise> finalExerciseList = new ArrayList<>();
                                for (WorkoutExerciseWithSeries ews : exercisesWithSeries) {
                                    WorkoutExercise exercise = ews.workoutExercise;
                                    exercise.setSeries(ews.series != null ? ews.series : new ArrayList<>());
                                    finalExerciseList.add(exercise);
                                }
                                routine.setWorkoutExercises(finalExerciseList);
                            }
                        }
                    }
                    training.setRoutinesList(new ArrayList<>(routines));
                    finalTrainingList.add(training);
                }

                trainingCallback.onSuccessFromLocal(finalTrainingList);

            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    public void createTraining(Training training) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            trainingDao.insert(training);
            getTrainings();
        });
    }

    public void updateTraining(Training training) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            trainingDao.update(training);
            getTrainings();
        });
    }

    // FIXED: Added Transaction to prevent Foreign Key crashes
    public void overwriteTrainings(List<Training> trainingList, String userId) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            //
            // TEMPORANEO
            //
            LocalDatabase.getDatabase(null).runInTransaction(() -> {
                // 1. Cancella i vecchi dati dell'utente per evitare conflitti
                trainingDao.deleteAllByUserId(userId);

                // 2. Itera su ogni training ricevuto
                for (Training training : trainingList) {
                    // Inserisci il training principale
                    trainingDao.insert(training);

                    if (training.getRoutinesList() != null) {
                        // 3. Itera sulle routine di ogni training
                        for (Routine routine : training.getRoutinesList()) {
                            // Associa la routine al training
                            routine.setTrainingId(training.getTrainingId());
                            routineDao.insert(routine);

                            if (routine.getWorkoutExercises() != null) {
                                // 4. Itera sugli esercizi di ogni routine
                                for (WorkoutExercise workoutExercise : routine.getWorkoutExercises()) {
                                    // Associa l'esercizio alla routine
                                    workoutExercise.setRoutineId(routine.getRoutineId());
                                    workoutExerciseDao.insert(workoutExercise);
                                    Log.e(TAG, "overwriteTrainings: workout exercise con id " + workoutExercise.getWorkoutExerciseId() + " inserita in workout routine " + routine.getRoutineId());

                                    if (workoutExercise.getSeries() != null) {
                                        // 5. Itera sulle serie di ogni esercizio
                                        for (Serie serie : workoutExercise.getSeries()) {
                                            // Associa la serie all'esercizio
                                            serie.setWorkoutExerciseId(workoutExercise.getWorkoutExerciseId());
                                            serieDao.insert(serie);
                                            Log.e(TAG, "overwriteTrainings: serie con id " + serie.getSerieId() + " inserita in workout exercise " + workoutExercise.getWorkoutExerciseId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 3. Refresh the UI data
                    getTrainings();
                    Log.d(TAG, "Database overwrite completed successfully");
                }

                // 6. Notifica che i dati sono stati aggiornati
                getTrainings();
            });
        });
    }

    public void resetDatabase(){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            serieDao.deteleAllSeries();
            workoutExerciseDao.deteleAllWorkoutExercises();
            routineDao.deteleAllRoutines();
            trainingDao.deleteAllTraings();
            Log.d(TAG, "Local database has been reset");
        });
    }
}