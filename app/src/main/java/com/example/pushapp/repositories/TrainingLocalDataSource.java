package com.example.pushapp.repositories;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Data source for handling training-related operations with the local Room database.
 * Executes database operations asynchronously and communicates results via callbacks.
 * Manages Trainings, Routines, and WorkoutExercises locally.
 */
public class TrainingLocalDataSource {
    private TrainingCallback trainingCallback;
    private final TrainingDao trainingDao;
    private final RoutineDao routineDao;
    private final WorkoutExerciseDao workoutExerciseDao;
    private final SerieDao serieDao;
    private final LocalDatabase localDatabase;

    /**
     * Constructs a new TrainingLocalDataSource.
     * Initializes the necessary DAOs.
     *
     * @param localDatabase The local Room database instance.
     */
    TrainingLocalDataSource(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.trainingDao = localDatabase.trainingDao();
        this.routineDao = localDatabase.routineDao();
        this.workoutExerciseDao = localDatabase.workoutExerciseDao();
        this.serieDao = localDatabase.serieDao();
        this.trainingCallback = null;
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param trainingCallback The callback implementation.
     */
    public void setTrainingCallback(TrainingCallback trainingCallback){
        this.trainingCallback = trainingCallback;
    }

    /**
     * Retrieves all trainings from the local database on a background thread.
     * Populates the trainings with their routines and exercises properly nested.
     */
    public void getTrainings(){
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
                    Collections.sort(routines, Comparator.comparing(routine -> routine.getCreatedAt()));
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

                trainingCallback.onSuccessFromLocalTrainingGet(finalTrainingList);

            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Fetches trainings for a specific user ID from the local database.
     * Similar to getTrainings but specifically triggers the 'fetch' callback success.
     *
     * @param userId The ID of the user.
     */
    public void fetchTrainings(String userId) {
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

                trainingCallback.onSuccessFromLocalTrainingFetch(userId, finalTrainingList);

            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Inserts a new training plan and its hierarchy (routines, exercises, series) into the local database.
     *
     * @param userId   The ID of the user owner.
     * @param training The Training object to insert.
     */
    public void createTraining(String userId, Training training) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                trainingDao.insert(training);
                if (training.getRoutinesList() != null) {
                    for (Routine routine : training.getRoutinesList()) {
                        routine.setTrainingId(training.getTrainingId());
                        routineDao.insert(routine);

                        if (routine.getWorkoutExercises() != null) {
                            for (WorkoutExercise workoutExercise : routine.getWorkoutExercises()) {
                                workoutExercise.setRoutineId(routine.getRoutineId());
                                workoutExerciseDao.insert(workoutExercise);

                                if (workoutExercise.getSeries() != null) {
                                    for (Serie serie : workoutExercise.getSeries()) {
                                        serie.setWorkoutExerciseId(workoutExercise.getWorkoutExerciseId());
                                        serieDao.insert(serie);
                                    }
                                }
                            }
                        }
                    }
                }
                trainingCallback.onSuccessFromLocalTrainingCreate(userId, training);
            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Updates an existing training plan's top-level details in the local database.
     *
     * @param training The Training object with updated info.
     */
    public void updateTraining(Training training) {
        try {
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                trainingDao.update(training);
                trainingCallback.onSuccessFromLocalTrainingUpdate(training);
            });
        }catch (Exception e){
            trainingCallback.onFailureFromLocal(e);
        }
    }

    /**
     * Overwrites all training data for a user with a new list of trainings.
     * Used when synchronizing from the remote source. Replaces existing local data atomically.
     *
     * @param trainingList The new list of trainings.
     * @param userId       The user ID.
     */
    public void overwriteTrainings(List<Training> trainingList, String userId) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            localDatabase.runInTransaction(() -> {
                try {
                    trainingDao.deleteAllByUserId(userId);

                    for (Training training : trainingList) {
                        trainingDao.insert(training);

                        if (training.getRoutinesList() != null) {
                            for (Routine routine : training.getRoutinesList()) {
                                routine.setTrainingId(training.getTrainingId());
                                routineDao.insert(routine);

                                if (routine.getWorkoutExercises() != null) {
                                    for (WorkoutExercise workoutExercise : routine.getWorkoutExercises()) {
                                        workoutExercise.setRoutineId(routine.getRoutineId());
                                        workoutExerciseDao.insert(workoutExercise);

                                        if (workoutExercise.getSeries() != null) {
                                            for (Serie serie : workoutExercise.getSeries()) {
                                                serie.setWorkoutExerciseId(workoutExercise.getWorkoutExerciseId());
                                                serieDao.insert(serie);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    trainingCallback.onFailureFromLocal(e);
                }
            });
            getTrainings();
        });
    }

    /**
     * Deletes a training plan from the local database.
     *
     * @param training The Training object to delete.
     */
    public void deleteTraining(Training training) {
        try {
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                trainingDao.delete(training);
                trainingCallback.onSuccessFromLocalTrainingDelete(training);
            });
        }catch (Exception e){
            trainingCallback.onFailureFromLocal(e);
        }
    }

    /**
     * Inserts a new routine into the local database.
     *
     * @param routine The Routine object to insert.
     */
    public void createRoutine(Routine routine){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                routineDao.insert(routine);
                trainingCallback.onSuccessFromLocalRoutineCreate(routine);
            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Updates an existing routine in the local database.
     * Employs a transaction to delete and re-insert the routine structure to ensure exercise list consistency.
     *
     * @param routine The Routine object with updated data.
     */
    public void updateRoutine(Routine routine){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                localDatabase.runInTransaction(() -> {
                    routineDao.delete(routine);
                    routineDao.insert(routine);
                    if (routine.getWorkoutExercises() != null) {
                        for (WorkoutExercise workoutExercise : routine.getWorkoutExercises()) {
                            workoutExercise.setRoutineId(routine.getRoutineId());
                            workoutExerciseDao.insert(workoutExercise);

                            if (workoutExercise.getSeries() != null) {
                                for (Serie serie : workoutExercise.getSeries()) {
                                    serie.setWorkoutExerciseId(workoutExercise.getWorkoutExerciseId());
                                    serieDao.insert(serie);
                                }
                            }
                        }
                    }
                });
                trainingCallback.onSuccessFromLocalRoutineUpdate(routine);
            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
                }
            });
    }

    /**
     * Deletes a routine from the local database.
     *
     * @param routine The Routine object to delete.
     */
    public void deleteRoutine(Routine routine){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            try {
                routineDao.delete(routine);
                trainingCallback.onSuccessFromLocalRoutineDelete(routine);
            } catch (Exception e) {
                trainingCallback.onFailureFromLocal(e);
            }
        });
    }

    /**
     * Clears all training-related data (series, exercises, routines, trainings) from the local database.
     */
    public void resetDatabase(){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            serieDao.deteleAllSeries();
            workoutExerciseDao.deteleAllWorkoutExercises();
            routineDao.deteleAllRoutines();
            trainingDao.deleteAllTraings();
        });
    }
}