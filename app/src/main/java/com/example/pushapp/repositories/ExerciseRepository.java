package com.example.pushapp.repositories;

import static com.example.pushapp.utils.Constants.DEBUG_MODE;

import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.utils.Constants;
import com.example.pushapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for managing Exercise data.
 * Coordinates between local storage (Room) and remote API to fetch and cache exercises.
 * Implements caching with a time-based expiration strategy.
 */
public class ExerciseRepository implements ExerciseCallback {

    private final ExerciseLocalDataSource exerciseLocalDataSource;
    private final ExerciseAPIDataSource exerciseAPIDataSource;
    private final ExerciseSampleDataSource exerciseSampleDataSource;
    private final MutableLiveData<Result> exercises;
    private final SessionManager sessionManager;

    /**
     * Constructs a new ExerciseRepository.
     * Initializes data sources and sets this class as the callback listener.
     *
     * @param exerciseLocalDataSource The local data source for exercises.
     * @param exerciseAPIDataSource   The remote API data source for exercises.
     * @param exerciseSampleDataSource The sample data source used for debug.
     * @param sessionManager          The session manager for handling cache timestamps.
     */
    ExerciseRepository(ExerciseLocalDataSource exerciseLocalDataSource, ExerciseAPIDataSource exerciseAPIDataSource,
                       ExerciseSampleDataSource exerciseSampleDataSource, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        exercises = new MutableLiveData<>();
        this.exerciseLocalDataSource = exerciseLocalDataSource;
        this.exerciseAPIDataSource = exerciseAPIDataSource;
        this.exerciseSampleDataSource = exerciseSampleDataSource;
        exerciseLocalDataSource.setCallback(this);
        exerciseAPIDataSource.setCallback(this);
        exerciseSampleDataSource.setCallback(this);
    }


    /**
     * Returns the LiveData observing the list of exercises.
     *
     * @return MutableLiveData containing the Result of exercise operations.
     */
    public MutableLiveData<Result> getExercises(){
        return exercises;
    }

    /**
     * Triggers a fetch of exercises.
     * Checks if the cache is expired or empty. If expired, fetches from API; otherwise loads from local database.
     */
    public void fetchExercises(){
        long lastFetchTime = sessionManager.getLastApiFetchTime();
        long timeSinceLastFetch = System.currentTimeMillis() - lastFetchTime;

        if(lastFetchTime == 0 || timeSinceLastFetch > Constants.API_FETCH_INTERVAL){
            if(DEBUG_MODE){
                exerciseSampleDataSource.getSampleExercises();
            } else {
                exerciseAPIDataSource.fetchAllExercises();
            }
            lastFetchTime = System.currentTimeMillis();
            sessionManager.saveApiFetchTime(lastFetchTime);
        } else {
            exerciseLocalDataSource.getExercises();
        }
    }

    /**
     * Resets the local exercise database and clears the fetch timestamp.
     * Used for clearing cache or full reset.
     */
    public void resetLocalDatabase(){
        exerciseLocalDataSource.deleteExercises();
        if(exercises != null){
            exercises.postValue(null);
            sessionManager.clearApiFetchTime();
        }
    }

    /**
     * Callback received when exercises are successfully fetched from the remote API.
     * Updates LiveData and caches the result locally.
     *
     * @param exerciseList The list of exercises fetched from the API.
     */
    @Override
    public void onSuccessFromRemote(ArrayList<Exercise> exerciseList) {
        exercises.postValue(new Result.ExerciseSuccess(exerciseList));
        exerciseLocalDataSource.insertExercises(exerciseList);
    }

    /**
     * Callback received when exercises are successfully retrieved from the local database.
     * Updates LiveData with the cached list.
     *
     * @param exerciseList The list of exercises retrieved locally.
     */
    @Override
    public void onSuccessFromLocalGet(List<Exercise> exerciseList) {
        exercises.postValue(new Result.ExerciseSuccess(exerciseList));
    }

    /**
     * Callback received when local exercises are successfully deleted.
     * Updates LiveData with an empty list.
     */
    @Override
    public void onSuccessFromLocalDelete() {
        exercises.postValue(new Result.ExerciseSuccess(new ArrayList<>()));
    }

    /**
     * Callback received when a remote API operation fails.
     * Falls back to loading data from the local database.
     *
     * @param e The exception causing the failure.
     */
    @Override
    public void onFailureFromRemote(Exception e) {
        exerciseLocalDataSource.getExercises();
    }

    /**
     * Callback received when a local database operation fails.
     * Posts an error to the LiveData.
     *
     * @param e The exception causing the failure.
     */
    @Override
    public void onFailureFromLocal(Exception e) {
        exercises.postValue(new Result.Error(e));
    }
}
