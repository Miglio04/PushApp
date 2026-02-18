package com.example.pushapp.repositories;

import android.content.Context;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.utils.SessionManager;

/**
 * Singleton Service Locator for managing dependency injection.
 * Provides singleton instances of repositories, data sources, and database managers throughout the application.
 */
public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private ServiceLocator() {}

    private volatile LocalDatabase localDatabase = null;
    private volatile TrainingRepository trainingRepository = null;
    private volatile ExerciseRepository exerciseRepository = null;
    private volatile SessionRepository sessionRepository = null;
    private volatile UserRepository userRepository = null;
    private volatile HistoryRepository historyRepository = null;

    private volatile SessionManager sessionManager = null;

    private volatile TrainingLocalDataSource trainingLocalDataSource = null;
    private volatile ExerciseLocalDataSource exerciseLocalDataSource = null;
    private volatile UserLocalDataSource userLocalDataSource = null;
    private volatile SessionLocalDataSource sessionLocalDataSource = null;
    private volatile HistoryLocalDataSource historyLocalDataSource = null;

    private volatile TrainingRemoteDataSource trainingRemoteDataSource = null;
    private volatile ExerciseAPIDataSource exerciseAPIDataSource = null;
    private volatile ExerciseSampleDataSource exerciseSampleDataSource = null;
    private volatile UserRemoteDataSource userRemoteDataSource = null;
    private volatile SessionRemoteDataSource sessionRemoteDataSource = null;
    private volatile HistoryRemoteDataSource historyRemoteDataSource = null;

    /**
     * Returns the singleton instance of ServiceLocator.
     *
     * @return The singleton ServiceLocator instance.
     */
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

    /**
     * Returns the singleton TrainingRepository instance.
     *
     * @param context The application context.
     * @return The TrainingRepository instance.
     */
    public synchronized TrainingRepository getTrainingRepository(Context context) {
        if(trainingRepository == null){
            trainingRepository = new TrainingRepository(
                    getTrainingLocalDataSource(context),
                    getTrainingRemoteDataSource()
            );
        }
        return trainingRepository;
    }

    /**
     * Returns the singleton ExerciseRepository instance.
     *
     * @param context The application context.
     * @return The ExerciseRepository instance.
     */
    public synchronized ExerciseRepository getExerciseRepository(Context context) {
        if(exerciseRepository == null){
            exerciseRepository = new ExerciseRepository(
                    getExerciseLocalDataSource(context),
                    getExerciseAPIDataSource(),
                    getExerciseSampleDataSource(),
                    getSessionManager(context)
            );
        }
        return exerciseRepository;
    }

    /**
     * Returns the singleton SessionRepository instance.
     *
     * @param context The application context.
     * @return The SessionRepository instance.
     */
    public synchronized SessionRepository getSessionRepository(Context context) {
        if(sessionRepository == null){
            sessionRepository = new SessionRepository(
                    getSessionLocalDataSource(),
                    getSessionRemoteDataSource()
            );
        }
        return sessionRepository;
    }

    /**
     * Returns the singleton UserRepository instance.
     *
     * @param context The application context.
     * @return The UserRepository instance.
     */
    public synchronized UserRepository getUserRepository(Context context) {
        if(userRepository == null){
            userRepository = new UserRepository(
                    getUserLocalDataSource(context),
                    getUserRemoteDataSource()
            );
        }
        return userRepository;
    }

    /**
     * Returns the singleton HistoryRepository instance.
     *
     * @param context The application context.
     * @return The HistoryRepository instance.
     */
    public synchronized HistoryRepository getHistoryRepository(Context context) {
        if (historyRepository == null) {
            historyRepository = new HistoryRepository(
                    getHistoryLocalDataSource(context),
                    getHistoryRemoteDataSource()
            );
        }
        return historyRepository;
    }

    /**
     * Returns the singleton SessionManager instance.
     *
     * @param context The application context.
     * @return The SessionManager instance.
     */
    public synchronized SessionManager getSessionManager(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context.getApplicationContext());
        }
        return sessionManager;
    }

    /**
     * Returns the TrainingLocalDataSource instance.
     *
     * @param context The application context.
     * @return The TrainingLocalDataSource instance.
     */
    public synchronized TrainingLocalDataSource getTrainingLocalDataSource(Context context) {
        if(trainingLocalDataSource == null){
            trainingLocalDataSource = new TrainingLocalDataSource(getDatabase(context));
        }
        return trainingLocalDataSource;
    }

    /**
     * Returns the ExerciseLocalDataSource instance.
     *
     * @param context The application context.
     * @return The ExerciseLocalDataSource instance.
     */
    public synchronized ExerciseLocalDataSource getExerciseLocalDataSource(Context context) {
        if(exerciseLocalDataSource == null){
            exerciseLocalDataSource = new ExerciseLocalDataSource(getDatabase(context));
        }
        return exerciseLocalDataSource;
    }

    /**
     * Returns the UserLocalDataSource instance.
     *
     * @param context The application context.
     * @return The UserLocalDataSource instance.
     */
    public synchronized UserLocalDataSource getUserLocalDataSource(Context context) {
        if(userLocalDataSource == null){
            userLocalDataSource = new UserLocalDataSource(getDatabase(context));
        }
        return userLocalDataSource;
    }

    /**
     * Returns the HistoryLocalDataSource instance.
     *
     * @param context The application context.
     * @return The HistoryLocalDataSource instance.
     */
    public synchronized HistoryLocalDataSource getHistoryLocalDataSource(Context context) {
        if (historyLocalDataSource == null) {
            historyLocalDataSource = new HistoryLocalDataSource(getDatabase(context));
        }
        return historyLocalDataSource;
    }

    /**
     * Returns the SessionLocalDataSource instance.
     *
     * @return The SessionLocalDataSource instance.
     */
    public synchronized SessionLocalDataSource getSessionLocalDataSource() {
        if(sessionLocalDataSource == null){
            sessionLocalDataSource = new SessionLocalDataSource();
        }
        return sessionLocalDataSource;
    }

    /**
     * Returns the TrainingRemoteDataSource instance.
     *
     * @return The TrainingRemoteDataSource instance.
     */
    public synchronized TrainingRemoteDataSource getTrainingRemoteDataSource() {
        if(trainingRemoteDataSource == null){
            trainingRemoteDataSource = new TrainingRemoteDataSource();
        }
        return trainingRemoteDataSource;
    }

    /**
     * Returns the ExerciseAPIDataSource instance.
     *
     * @return The ExerciseAPIDataSource instance.
     */
    public synchronized ExerciseAPIDataSource getExerciseAPIDataSource() {
        if(exerciseAPIDataSource == null){
            exerciseAPIDataSource = new ExerciseAPIDataSource();
        }
        return exerciseAPIDataSource;
    }

    /**
     * Returns the ExerciseSampleDataSource instance.
     *
     * @return The ExerciseSampleDataSource instance.
     */
    public synchronized ExerciseSampleDataSource getExerciseSampleDataSource() {
        if(exerciseSampleDataSource == null){
            exerciseSampleDataSource = new ExerciseSampleDataSource();
        }
        return exerciseSampleDataSource;
    }

    /**
     * Returns the UserRemoteDataSource instance.
     *
     * @return The UserRemoteDataSource instance.
     */
    public synchronized UserRemoteDataSource getUserRemoteDataSource() {
        if(userRemoteDataSource == null){
            userRemoteDataSource = new UserRemoteDataSource();
        }
        return userRemoteDataSource;
    }

    /**
     * Returns the SessionRemoteDataSource instance.
     *
     * @return The SessionRemoteDataSource instance.
     */
    public synchronized SessionRemoteDataSource getSessionRemoteDataSource() {
        if(sessionRemoteDataSource == null){
            sessionRemoteDataSource = new SessionRemoteDataSource();
        }
        return sessionRemoteDataSource;
    }

    /**
     * Returns the HistoryRemoteDataSource instance.
     *
     * @return The HistoryRemoteDataSource instance.
     */
    public synchronized HistoryRemoteDataSource getHistoryRemoteDataSource() {
        if (historyRemoteDataSource == null) {
            historyRemoteDataSource = new HistoryRemoteDataSource();
        }
        return historyRemoteDataSource;
    }

    /**
     * Returns the singleton LocalDatabase instance.
     *
     * @param context The application context.
     * @return The LocalDatabase instance.
     */
    private synchronized LocalDatabase getDatabase(Context context) {
        if (localDatabase == null) {
            localDatabase = LocalDatabase.getDatabase(context.getApplicationContext());
        }
        return localDatabase;
    }
}