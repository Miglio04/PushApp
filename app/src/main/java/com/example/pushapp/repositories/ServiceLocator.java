package com.example.pushapp.repositories;

import android.content.Context;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.utils.SessionManager;

public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private ServiceLocator() {}

    private volatile LocalDatabase localDatabase = null;
    private volatile TrainingRepository trainingRepository = null;
    private volatile ExerciseRepository exerciseRepository = null;
    private volatile SessionRepository sessionRepository = null;
    private volatile UserRepository userRepository = null;
    private volatile HistoryRepository historyRepository = null;

    // Riferimento al gestore della sessione temporanea (Punto 9)
    private volatile SessionManager sessionManager = null;

    private volatile TrainingLocalDataSource trainingLocalDataSource = null;
    private volatile ExerciseLocalDataSource exerciseLocalDataSource = null;
    private volatile UserLocalDataSource userLocalDataSource = null;
    private volatile SessionLocalDataSource sessionLocalDataSource = null;
    private volatile HistoryLocalDataSource historyLocalDataSource = null;

    private volatile TrainingRemoteDataSource trainingRemoteDataSource = null;
    private volatile ExerciseAPIDataSource exerciseAPIDataSource = null;
    private volatile UserRemoteDataSource userRemoteDataSource = null;
    private volatile SessionRemoteDataSource sessionRemoteDataSource = null;
    private volatile HistoryRemoteDataSource historyRemoteDataSource = null;

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

    // --- REPOSITORY GETTERS ---

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
                    getExerciseAPIDataSource(),
                    getSessionManager(context)
            );
        }
        return exerciseRepository;
    }

    public synchronized SessionRepository getSessionRepository(Context context) {
        if(sessionRepository == null){
            sessionRepository = new SessionRepository(
                    getSessionLocalDataSource(),
                    getSessionRemoteDataSource()
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

    public synchronized HistoryRepository getHistoryRepository(Context context) {
        if (historyRepository == null) {
            historyRepository = new HistoryRepository(
                    getHistoryLocalDataSource(context),
                    getHistoryRemoteDataSource()
            );
        }
        return historyRepository;
    }

    // NUOVO: Getter per il SessionManager (Punto 9)
    public synchronized SessionManager getSessionManager(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context.getApplicationContext());
        }
        return sessionManager;
    }

    // --- LOCAL DATA SOURCE GETTERS ---

    public synchronized TrainingLocalDataSource getTrainingLocalDataSource(Context context) {
        if(trainingLocalDataSource == null){
            trainingLocalDataSource = new TrainingLocalDataSource(getDatabase(context));
        }
        return trainingLocalDataSource;
    }

    public synchronized ExerciseLocalDataSource getExerciseLocalDataSource(Context context) {
        if(exerciseLocalDataSource == null){
            exerciseLocalDataSource = new ExerciseLocalDataSource(getDatabase(context));
        }
        return exerciseLocalDataSource;
    }

    public synchronized UserLocalDataSource getUserLocalDataSource(Context context) {
        if(userLocalDataSource == null){
            userLocalDataSource = new UserLocalDataSource(getDatabase(context));
        }
        return userLocalDataSource;
    }

    public synchronized HistoryLocalDataSource getHistoryLocalDataSource(Context context) {
        if (historyLocalDataSource == null) {
            historyLocalDataSource = new HistoryLocalDataSource(getDatabase(context));
        }
        return historyLocalDataSource;
    }

    public synchronized SessionLocalDataSource getSessionLocalDataSource() {
        if(sessionLocalDataSource == null){
            sessionLocalDataSource = new SessionLocalDataSource();
        }
        return sessionLocalDataSource;
    }

    // --- REMOTE DATA SOURCE GETTERS ---

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

    public synchronized UserRemoteDataSource getUserRemoteDataSource() {
        if(userRemoteDataSource == null){
            userRemoteDataSource = new UserRemoteDataSource();
        }
        return userRemoteDataSource;
    }

    public synchronized SessionRemoteDataSource getSessionRemoteDataSource() {
        if(sessionRemoteDataSource == null){
            sessionRemoteDataSource = new SessionRemoteDataSource();
        }
        return sessionRemoteDataSource;
    }

    public synchronized HistoryRemoteDataSource getHistoryRemoteDataSource() {
        if (historyRemoteDataSource == null) {
            historyRemoteDataSource = new HistoryRemoteDataSource();
        }
        return historyRemoteDataSource;
    }

    // --- DATABASE ---

    private synchronized LocalDatabase getDatabase(Context context) {
        if (localDatabase == null) {
            localDatabase = LocalDatabase.getDatabase(context.getApplicationContext());
        }
        return localDatabase;
    }
}