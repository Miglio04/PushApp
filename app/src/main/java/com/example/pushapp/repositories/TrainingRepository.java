package com.example.pushapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for managing Training and Routine data.
 * Acts as a mediator between the local Room database and the remote Firestore source,
 * handling data synchronization and exposing results via LiveData.
 */
public class TrainingRepository implements TrainingCallback{
    private ListenerRegistration trainingsListener;
    private final TrainingLocalDataSource trainingLocalDataSource;
    private final TrainingRemoteDataSource trainingRemoteDataSource;
    private final MutableLiveData<Result> trainingList;

    /**
     * Constructs a new TrainingRepository.
     * Initializes data sources and sets this class as the callback listener for both.
     *
     * @param trainingLocalDataSource The local data source (Room).
     * @param trainingRemoteDataSource The remote data source (Firestore).
     */
    TrainingRepository(TrainingLocalDataSource trainingLocalDataSource, TrainingRemoteDataSource trainingRemoteDataSource) {
        trainingList = new MutableLiveData<>();
        this.trainingLocalDataSource = trainingLocalDataSource;
        this.trainingRemoteDataSource = trainingRemoteDataSource;
        trainingLocalDataSource.setTrainingCallback(this);
        trainingRemoteDataSource.setTrainingCallback(this);
    }

    /**
     * Returns the LiveData observing the list of trainings.
     * @return LiveData containing the result of training operations.
     */
    public LiveData<Result> getTrainingList(){
        return trainingList;
    }

    /**
     * Detaches the Firestore listener if it is currently active.
     * Should be called when the repository is no longer needed to prevent leaks.
     */
    public void detachTrainingsListener() {
        if (trainingsListener != null) {
            trainingsListener.remove();
            trainingsListener = null;
        }
    }

    /**
     * Triggers a fetch of trainings for a specific user from the local database.
     * If the local database is empty, it may trigger a remote fetch.
     *
     * @param userId The ID of the user.
     */
    public void fetchTrainings(String userId){
        trainingLocalDataSource.fetchTrainings(userId);
    }

    /**
     * Creates a new training plan in the local database.
     * Success will trigger a sync to the remote source.
     *
     * @param userId   The user ID associated with the training.
     * @param training The Training object to create.
     */
    public void createTraining(String userId, Training training) {
        trainingLocalDataSource.createTraining(userId, training);
    }

    /**
     * Updates an existing training plan in both local and remote sources.
     *
     * @param training The updated Training object.
     */
    public void updateTraining(Training training){
        if(training != null){
            trainingLocalDataSource.updateTraining(training);
            trainingRemoteDataSource.updateTraining(training);
        }
    }

    /**
     * Deletes a training plan from the local database.
     * Success will trigger a remote deletion.
     *
     * @param training The Training object to delete.
     */
    public void deleteTraining(Training training) {
        trainingLocalDataSource.deleteTraining(training);
    }

    /**
     * Creates a new routine (day) within a training plan in the local database.
     *
     * @param routine The Routine object to insert.
     */
    public void createRoutine(Routine routine){
        if(routine != null){
            trainingLocalDataSource.createRoutine(routine);
        }
    }

    /**
     * Updates an existing routine in the local database.
     *
     * @param routine The Routine object with updated data.
     */
    public void updateRoutine(Routine routine){
        if(routine != null){
            trainingLocalDataSource.updateRoutine(routine);
        }
    }

    /**
     * Deletes a routine from the local database.
     *
     * @param routine The Routine object to delete.
     */
    public void deleteRoutine(Routine routine){
        if(routine != null){
            trainingLocalDataSource.deleteRoutine(routine);
        }
    }

    /**
     * Callback received when trainings are successfully fetched from local storage.
     * Updates LiveData and triggers remote fetch if local data is empty.
     *
     * @param userId The user ID.
     * @param trainingListSuccess The list of trainings retrieved.
     */
    public void onSuccessFromLocalTrainingFetch(String userId, List<Training> trainingListSuccess) {
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<>(trainingListSuccess));
        trainingList.postValue(result);

        if(trainingListSuccess.isEmpty()){
            trainingRemoteDataSource.fetchTrainings(userId);
        }
    }

    /**
     * Callback received when trainings are refreshed from local storage.
     * Updates the LiveData with the new list.
     *
     * @param trainingListSuccess The refreshed list of trainings.
     */
    public void onSuccessFromLocalTrainingGet(List<Training> trainingListSuccess){
        Result.TrainingsSuccess result = new Result.TrainingsSuccess(new ArrayList<>(trainingListSuccess));
        trainingList.postValue(result);
    }

    /**
     * Callback received when a training is successfully created locally.
     * Triggers creation in the remote source and refreshes the local list.
     *
     * @param userId   The user ID.
     * @param training The created Training.
     */
    public void onSuccessFromLocalTrainingCreate(String userId, Training training){
        trainingRemoteDataSource.createTraining(userId, training);
        trainingLocalDataSource.getTrainings();
    }

    /**
     * Callback received when a training is successfully deleted locally.
     * Triggers deletion in the remote source and refreshes the local list.
     *
     * @param training The deleted Training.
     */
    public void onSuccessFromLocalTrainingDelete(Training training){
        if(training != null) {
            trainingRemoteDataSource.deleteTraining(training);
            trainingLocalDataSource.getTrainings();
        }
    }

    /**
     * Callback received when a training is successfully updated locally.
     * Triggers update in the remote source and refreshes the local list.
     *
     * @param training The updated Training.
     */
    public void onSuccessFromLocalTrainingUpdate(Training training){
        if(training != null) {
            trainingRemoteDataSource.updateTraining(training);
            trainingLocalDataSource.getTrainings();
        }
    }

    /**
     * Callback received when a routine is successfully created locally.
     * Syncs with remote and refreshes list.
     *
     * @param routine The created Routine.
     */
    public void onSuccessFromLocalRoutineCreate(Routine routine){
        if(routine != null) {
            trainingRemoteDataSource.createRoutine(routine);
            trainingLocalDataSource.getTrainings();
        }
    }

    /**
     * Callback received when a routine is successfully updated locally.
     * Syncs with remote and refreshes list.
     *
     * @param routine The updated Routine.
     */
    public void onSuccessFromLocalRoutineUpdate(Routine routine){
        if(routine != null) {
            trainingRemoteDataSource.updateRoutine(routine);
            trainingLocalDataSource.getTrainings();
        }
    }

    /**
     * Callback received when a routine is successfully deleted locally.
     * Syncs with remote and refreshes list.
     *
     * @param routine The deleted Routine.
     */
    public void onSuccessFromLocalRoutineDelete(Routine routine){
        if(routine != null) {
            trainingRemoteDataSource.deleteRoutine(routine);
            trainingLocalDataSource.getTrainings();
        }
    }

    /**
     * Callback received when trainings are successfully fetched from the remote source.
     * Overwrites the local database with the fresh remote data.
     *
     * @param trainingListSuccess The list of trainings from remote.
     * @param userId The user ID.
     */
    public void onSuccessFromRemote(List<Training> trainingListSuccess, String userId) {
        trainingLocalDataSource.overwriteTrainings(trainingListSuccess, userId);
    }

    /**
     * Callback received when a local database operation fails.
     * Posts an error result to LiveData.
     *
     * @param exception The exception causing the failure.
     */
    public void onFailureFromLocal(Exception exception) {
        Result.Error resultError = new Result.Error(exception.getMessage());
        trainingList.postValue(resultError);
    }

    /**
     * Callback received when a remote operation fails.
     * Posts an error result to LiveData.
     *
     * @param exception The exception causing the failure.
     */
    public void onFailureFromRemote(Exception exception){
        Result.Error resultError = new Result.Error(exception.getMessage());
        trainingList.postValue(resultError);
    }

    /**
     * Resets the local database by deleting all training data.
     * Updates LiveData to empty list state accordingly.
     */
    public void resetLocalDatabase(){
        try{
            trainingLocalDataSource.resetDatabase();
            if(trainingList != null){
                trainingList.postValue(new Result.TrainingsSuccess(new java.util.ArrayList<>()));
            }
        }catch (Exception e){
            Result.Error resultError = new Result.Error(e.getMessage());
            trainingList.postValue(resultError);
        }
    }
}
