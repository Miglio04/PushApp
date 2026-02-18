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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrainingLocalDataSource {
    private final String TAG = "TrainingLocalDataSource";
    private TrainingCallback trainingCallback;
    private final TrainingDao trainingDao;
    private final RoutineDao routineDao;
    private final WorkoutExerciseDao workoutExerciseDao;
    private final SerieDao serieDao;
    private final LocalDatabase localDatabase;

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