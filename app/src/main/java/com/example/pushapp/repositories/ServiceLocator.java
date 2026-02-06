package com.example.pushapp.repositories;

import android.content.Context;

import com.example.pushapp.database.LocalDatabase;
// --- NUOVI IMPORT ---
import com.example.pushapp.utils.SessionManager;
import com.example.pushapp.database.HistoryDao;

public class ServiceLocator {

    private static volatile ServiceLocator instance = null;

    private ServiceLocator() {};

    private volatile LocalDatabase localDatabase = null;

    // --- REPOSITORIES ESISTENTI ---
    private volatile TrainingRepository trainingRepository = null;
    private volatile ExerciseRepository exerciseRepository = null;
    private volatile SessionRepository sessionRepository = null;
    private volatile UserRepository userRepository = null;

    // --- NUOVI REPOSITORIES E MANAGER ---
    private volatile HistoryRepository historyRepository = null;
    private volatile SessionManager sessionManager = null;

    // --- DATA SOURCES ESISTENTI ---
    private volatile TrainingLocalDataSource trainingLocalDataSource = null;
    private volatile ExerciseLocalDataSource exerciseLocalDataSource = null;
    private volatile UserLocalDataSource userLocalDataSource = null;
    private volatile SessionLocalDataSource sessionLocalDataSource = null;
    private volatile TrainingRemoteDataSource trainingRemoteDataSource = null;
    private volatile ExerciseAPIDataSource exerciseAPIDataSource = null;
    private volatile UserRemoteDataSource userRemoteDataSource = null;
    private volatile SessionRemoteDataSource sessionRemoteDataSource = null;

    // --- NUOVI DATA SOURCES ---
    private volatile HistoryLocalDataSource historyLocalDataSource = null;
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

    // =========================================================================
    //  REPOSITORIES
    // =========================================================================

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

    // --- NUOVO: HISTORY REPOSITORY ---
    public synchronized HistoryRepository getHistoryRepository(Context context) {
        if (historyRepository == null) {
            historyRepository = HistoryRepository.getInstance(
                    getHistoryLocalDataSource(context),
                    getHistoryRemoteDataSource()
            );
        }
        return historyRepository;
    }

    // --- NUOVO: SESSION MANAGER (ANTI-CRASH) ---
    public synchronized SessionManager getSessionManager(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
        return sessionManager;
    }


    // =========================================================================
    //  LOCAL DATA SOURCES
    // =========================================================================

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

    // --- NUOVO: HISTORY LOCAL DATA SOURCE ---
    public synchronized HistoryLocalDataSource getHistoryLocalDataSource(Context context) {
        if (historyLocalDataSource == null) {
            // Otteniamo il database e poi il DAO specifico
            LocalDatabase db = getDatabase(context);
            HistoryDao historyDao = db.historyDao();
            historyLocalDataSource = new HistoryLocalDataSource(historyDao);
        }
        return historyLocalDataSource;
    }


    // =========================================================================
    //  REMOTE DATA SOURCES
    // =========================================================================

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

    public synchronized SessionLocalDataSource getSessionLocalDataSource() {
        if(sessionLocalDataSource == null){
            sessionLocalDataSource = new SessionLocalDataSource();
        }
        return sessionLocalDataSource;
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

    // --- NUOVO: HISTORY REMOTE DATA SOURCE ---
    public synchronized HistoryRemoteDataSource getHistoryRemoteDataSource() {
        if (historyRemoteDataSource == null) {
            historyRemoteDataSource = new HistoryRemoteDataSource();
        }
        return historyRemoteDataSource;
    }


    // =========================================================================
    //  DATABASE
    // =========================================================================

    private synchronized LocalDatabase getDatabase(Context context) {
        if (localDatabase == null) {
            localDatabase = LocalDatabase.getDatabase(context.getApplicationContext());
        }
        return localDatabase;
    }
}