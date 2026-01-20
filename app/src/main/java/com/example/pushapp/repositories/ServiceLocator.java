package com.example.pushapp.repositories;

import android.content.Context;

import com.example.pushapp.database.LocalDatabase;

public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private ServiceLocator() {};

    private volatile LocalDatabase localDatabase = null;
    private volatile TrainingRepository trainingRepository = null;
    private volatile ExerciseRepository exerciseRepository = null;
    private volatile SessionRepository sessionRepository = null;
    private volatile UserRepository userRepository = null;
    private volatile TrainingLocalDataSource trainingLocalDataSource = null;
    private volatile ExerciseLocalDataSource exerciseLocalDataSource = null;
    private volatile UserLocalDataSource userLocalDataSource = null;
    private volatile TrainingRemoteDataSource trainingRemoteDataSource = null;
    private volatile ExerciseAPIDataSource exerciseAPIDataSource = null;
    private volatile SessionDataSource sessionDataSource = null;
    private volatile UserRemoteDataSource userRemoteDataSource = null;


    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    // Repositories

    public synchronized TrainingRepository getTrainingRepository(Context context) {
        if(trainingRepository == null){
            trainingRepository = new TrainingRepository(
                    getTrainingLocalDataSource(context),
                    getTrainingRemoteDataSource()
            );
        }
        return trainingRepository;
    }

    public synchronized ExerciseRepository getExerciseRepository(Context context) {
        if(exerciseRepository == null){
            exerciseRepository = new ExerciseRepository(
                    getExerciseLocalDataSource(context),
                    getExerciseAPIDataSource()
            );
        }
        return exerciseRepository;
    }

    public synchronized SessionRepository getSessionRepository(Context context) {
        if(sessionRepository == null){
            sessionRepository = new SessionRepository(
                    getSessionDataSource()
            );
        }
        return sessionRepository;
    }

    public synchronized UserRepository getUserRepository(Context context) {
        if(userRepository == null){
            userRepository = new UserRepository(
                    getUserLocalDataSource(context),
                    getUserRemoteDataSource(),
                    getSessionRepository(context)
            );
        }
        return userRepository;
    }

    // Local Data Sources

    public synchronized TrainingLocalDataSource getTrainingLocalDataSource(Context context) {
        if(trainingLocalDataSource == null){
            LocalDatabase localDatabase = getDatabase(context);
            trainingLocalDataSource = new TrainingLocalDataSource(localDatabase);
        }
        return trainingLocalDataSource;
    }

    public synchronized ExerciseLocalDataSource getExerciseLocalDataSource(Context context) {
        if(exerciseLocalDataSource == null){
            LocalDatabase localDatabase = getDatabase(context);
            exerciseLocalDataSource = new ExerciseLocalDataSource(localDatabase);
        }
        return exerciseLocalDataSource;
    }

    public synchronized UserLocalDataSource getUserLocalDataSource(Context context) {
        if(userLocalDataSource == null){
            LocalDatabase localDatabase = getDatabase(context);
            userLocalDataSource = new UserLocalDataSource(localDatabase);
        }
        return userLocalDataSource;
    }

    // Remote Data Sources

    public synchronized TrainingRemoteDataSource getTrainingRemoteDataSource() {
        if(trainingRemoteDataSource == null){
            trainingRemoteDataSource = new TrainingRemoteDataSource();
        }
        return trainingRemoteDataSource;
    }

    public synchronized ExerciseAPIDataSource getExerciseAPIDataSource() {
        if(exerciseAPIDataSource == null){
            exerciseAPIDataSource = new ExerciseAPIDataSource();
        }
        return exerciseAPIDataSource;
    }

    public synchronized SessionDataSource getSessionDataSource() {
        if(sessionDataSource == null){
            sessionDataSource = new SessionDataSource();
        }
        return sessionDataSource;
    }

    public synchronized UserRemoteDataSource getUserRemoteDataSource() {
        if(userRemoteDataSource == null){
            userRemoteDataSource = new UserRemoteDataSource();
        }
        return userRemoteDataSource;
    }

    // Database

    private synchronized LocalDatabase getDatabase(Context context) {
        if (localDatabase == null) {
            localDatabase = LocalDatabase.getDatabase(context.getApplicationContext());
        }
        return localDatabase;
    }

}
