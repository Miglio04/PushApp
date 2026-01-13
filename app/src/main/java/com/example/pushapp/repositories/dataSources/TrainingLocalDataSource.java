package com.example.pushapp.repositories.dataSources;

import com.example.pushapp.database.ExerciseDao;
import com.example.pushapp.database.SerieDao;
import com.example.pushapp.database.TrainingDao;
import com.example.pushapp.database.TrainingDayDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.Training;
import com.example.pushapp.repositories.TrainingCallback;

import java.util.List;

public class TrainingLocalDataSource {
    private TrainingCallback trainingCallback;
    private final TrainingDao trainingDao;
    private TrainingDayDao trainingDayDao;
    private ExerciseDao exerciseDao;
    private SerieDao serieDao;


    public TrainingLocalDataSource(LocalDatabase localDatabase) {
        this.trainingDao = localDatabase.trainingDao();
        this.trainingDayDao = localDatabase.trainingDayDao();
        this.exerciseDao = localDatabase.exerciseDao();
        this.serieDao = localDatabase.serieDao();
        this.trainingCallback = null;
    }

    public void setTrainingCallback(TrainingCallback trainingCallback){
        this.trainingCallback = trainingCallback;
    }

    public void getTrainings(){
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            List<Training> trainingListResult= trainingDao.getAllTrainings();
            if (trainingListResult != null) {
                trainingCallback.onSuccessFromLocal(trainingListResult);
            }else{
                trainingCallback.onFailureFromLocal(new Exception("Training list is null"));
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

    public void overwriteTrainigs(List<Training> trainingList, String userId) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            trainingDao.deleteAllByUserId(userId);
            trainingDao.insertAll(trainingList);
            getTrainings();
        });
    }
}
