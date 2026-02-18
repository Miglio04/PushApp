package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel responsible for managing training plans, routines, and exercises.
 * Handles CRUD operations for trainings and routines, as well as exercise filtering and modification logic.
 */
public class TrainingViewModel extends ViewModel {
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final LiveData<Result> trainings;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<Routine> editableRoutine = new MutableLiveData<>();

    private final LiveData<Result> availableExercises;
    private final MediatorLiveData<Result> filteredAvailableExercises = new MediatorLiveData<>();

    private final MutableLiveData<List<String>> availableMuscleGroups = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableDifficulties = new MutableLiveData<>();

    private List<Exercise> fullExerciseList = new ArrayList<>();

    /**
     * Constructor for TrainingViewModel.
     *
     * @param trainingRepository Repository for training data operations.
     * @param exerciseRepository Repository for exercise data operations.
     */
    public TrainingViewModel(TrainingRepository trainingRepository, ExerciseRepository exerciseRepository){
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.trainings = trainingRepository.getTrainingList();
        this.availableExercises = exerciseRepository.getExercises();

        this.filteredAvailableExercises.addSource(this.availableExercises, result -> {
            if (result == null) {
                filteredAvailableExercises.setValue(null);
                return;
            }
            if (result.isExerciseSuccess()) {
                fullExerciseList = ((Result.ExerciseSuccess) result).getData();
                filteredAvailableExercises.setValue(result);
                extractFilterCategories(fullExerciseList);
            } else {
                filteredAvailableExercises.setValue(result);
            }
        });
    }

    public LiveData<Result> getTrainings() { return trainings; }
    public LiveData<Routine> getEditableRoutine() { return editableRoutine; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Result> getAvailableExercises() { return availableExercises; }
    public LiveData<Result> getFilteredAvailableExercises() { return filteredAvailableExercises; }
    public LiveData<List<String>> getAvailableMuscleGroups() { return availableMuscleGroups; }
    public LiveData<List<String>> getAvailableDifficulties() { return availableDifficulties; }

    /**
     * Creates a new empty training plan.
     *
     * @param userId The ID of the user.
     */
    public void createTraining(String userId) {
        isLoading.setValue(true);
        Training training = new Training();
        training.setUserId(userId);
        training.setName("New Training");
        training.setDescription("Description");
        trainingRepository.createTraining(userId, training);
    }

    /**
     * Fetches the list of training plans for the specified user.
     *
     * @param userId The ID of the user.
     */
    public void fetchTrainings(String userId){
        trainingRepository.fetchTrainings(userId);
    }

    /**
     * Updates an existing training plan.
     *
     * @param training The updated Training object.
     */
    public void updateTraining(Training training) {
        trainingRepository.updateTraining(training);
    }

    /**
     * Deletes a training plan.
     *
     * @param training The Training object to delete.
     */
    public void deleteTraining(Training training) {
        trainingRepository.deleteTraining(training);
    }

    /**
     * Creates a new routine within an existing training plan.
     *
     * @param currentTraining The parent training plan.
     */
    public void createRoutine(Training currentTraining) {
        Routine routine = new Routine();
        routine.setName("New Routine");
        routine.setTrainingId(currentTraining.getTrainingId());
        routine.setUserId(currentTraining.getUserId());
        trainingRepository.createRoutine(routine);
    }

    /**
     * Updates an existing routine.
     *
     * @param routine The updated Routine object.
     */
    public void updateRoutine(Routine routine){
        trainingRepository.updateRoutine(routine);
    }

    /**
     * Deletes a routine.
     *
     * @param routine The Routine object to delete.
     */
    public void deleteRoutine(Routine routine) {
        trainingRepository.deleteRoutine(routine);
    }

    /**
     * Loads a specific routine for editing.
     *
     * @param trainingId The ID of the training plan containing the routine.
     * @param routineId  The ID of the routine to load.
     */
    public void loadRoutineForEdit(String trainingId, String routineId) {
        isLoading.setValue(true);

        if (trainingId == null || routineId == null) {
            errorMessage.setValue("ID mancante per il caricamento.");
            isLoading.setValue(false);
            return;
        }

        if (trainings.getValue() != null && trainings.getValue().isTrainingsSuccess()) {
            List<Training> currentTrainings = ((Result.TrainingsSuccess) trainings.getValue()).getData();
            if (currentTrainings != null) {
                for (Training t : currentTrainings) {
                    if (trainingId.equals(t.getTrainingId()) && t.getRoutinesList() != null) {
                        for (Routine routine : t.getRoutinesList()) {
                            if (routineId.equals(routine.getRoutineId())) {
                                Routine routineTemp = new Routine(routine);
                                editableRoutine.setValue(routineTemp); // Pubblica il giorno reale
                                isLoading.setValue(false);
                                return;
                            }
                        }
                    }
                }
                errorMessage.setValue("Routine non trovata.");
                isLoading.setValue(false);
            }

        }
    }


    /**
     * Loads the list of available exercises.
     * If exercises are cached, uses the cache; otherwise fetches from the repository.
     */
    public void loadAvailableExercises() {
        if (!fullExerciseList.isEmpty()) {
            if (filteredAvailableExercises.getValue() == null || (filteredAvailableExercises.getValue().isExerciseSuccess() && ((Result.ExerciseSuccess)filteredAvailableExercises.getValue()).getData().isEmpty())) {
                filteredAvailableExercises.setValue(new Result.ExerciseSuccess(fullExerciseList));
            }
            return;
        }
        exerciseRepository.fetchExercises();
    }

    /**
     * Extracts unique muscle groups and difficulty levels from the exercise list for filtering.
     *
     * @param exercises The list of exercises to extract categories from.
     */
    private void extractFilterCategories(List<Exercise> exercises) {
        Set<String> muscleSet = new HashSet<>();
        Set<String> difficultySet = new HashSet<>();

        for (Exercise exercise : exercises) {
            if (exercise.getMuscle() != null && !exercise.getMuscle().isEmpty()) {
                muscleSet.add(capitalize(exercise.getMuscle()));
            }
            if (exercise.getDifficulty() != null && !exercise.getDifficulty().isEmpty()) {
                difficultySet.add(capitalize(exercise.getDifficulty()));
            }
        }

        List<String> sortedMuscles = new ArrayList<>(muscleSet);
        Collections.sort(sortedMuscles);
        availableMuscleGroups.setValue(sortedMuscles);

        List<String> sortedDifficulties = new ArrayList<>(difficultySet);
        Collections.sort(sortedDifficulties);
        availableDifficulties.setValue(sortedDifficulties);
    }

    /**
     * Filters available exercises based on search query, muscle group, and difficulty.
     *
     * @param query           The search query string.
     * @param muscleGroup     The selected muscle group filter.
     * @param difficultyGroup The selected difficulty level filter.
     */
    public void applyFilters(String query, String muscleGroup, String difficultyGroup) {
        List<Exercise> tempFilteredList = new ArrayList<>(fullExerciseList);
        List<Exercise> nextStageList = new ArrayList<>();

        String muscleFilter = (muscleGroup == null) ? "All" : muscleGroup;
        if (muscleFilter.equalsIgnoreCase("All") || muscleFilter.equalsIgnoreCase("Tutti")) {
            nextStageList.addAll(tempFilteredList);
        } else {
            for (Exercise exercise : tempFilteredList) {
                if (exercise.getMuscle() != null && capitalize(exercise.getMuscle()).equalsIgnoreCase(muscleFilter)) {
                    nextStageList.add(exercise);
                }
            }
        }
        tempFilteredList = new ArrayList<>(nextStageList);
        nextStageList.clear();

        String diffFilter = (difficultyGroup == null) ? "All" : difficultyGroup;
        if (diffFilter.equalsIgnoreCase("All") || diffFilter.equalsIgnoreCase("Tutti")) {
            nextStageList.addAll(tempFilteredList);
        } else {
            for (Exercise exercise : tempFilteredList) {
                if (exercise.getDifficulty() != null && capitalize(exercise.getDifficulty()).equalsIgnoreCase(diffFilter)) {
                    nextStageList.add(exercise);
                }
            }
        }
        tempFilteredList = new ArrayList<>(nextStageList);
        nextStageList.clear();

        if (query == null || query.isEmpty()) {
            filteredAvailableExercises.setValue(new Result.ExerciseSuccess(tempFilteredList));
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Exercise exercise : tempFilteredList) {
                if (exercise.getName().toLowerCase().contains(lowerCaseQuery)) {
                    nextStageList.add(exercise);
                }
            }
            filteredAvailableExercises.setValue(new Result.ExerciseSuccess(nextStageList));
        }
    }

    /**
     * Capitalizes the first letter of a string and replaces underscores with spaces.
     *
     * @param str The string to format.
     * @return The formatted string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String formatted = str.replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }


    /**
     * Creates a default list of series (sets) for a new exercise.
     *
     * @return A list of default Serie objects.
     */
    private List<Serie> createDefaultSeries() {
        List<Serie> series = new ArrayList<>();
        // Crea 4 serie di default
        for (int i = 0; i < 4; i++) {
            Serie defaultSet = new Serie();
            defaultSet.setTargetWeight(10.0);
            defaultSet.setTargetReps(6);
            series.add(defaultSet);
        }
        return series;
    }

    /**
     * Adds an exercise to the currently editable routine.
     *
     * @param workoutExercise The exercise to add.
     */
    public void addExerciseToRoutine(WorkoutExercise workoutExercise) {
        Routine routine = editableRoutine.getValue();
        if (routine != null) {
            List<WorkoutExercise> currentList = routine.getWorkoutExercises();
            if (currentList == null) currentList = new ArrayList<>();

            List<WorkoutExercise> updatedList = new ArrayList<>(currentList);

            if (workoutExercise.getSeries() == null || workoutExercise.getSeries().isEmpty()) {
                workoutExercise.setSeries(createDefaultSeries());
            }

            updatedList.add(workoutExercise);
            routine.setWorkoutExercises(updatedList);
            editableRoutine.setValue(routine);
        } else {
            errorMessage.setValue("Errore: Giorno non caricato.");
        }
    }

    /**
     * Replaces an exercise in the routine at the specified position with a new one.
     *
     * @param position        The index of the exercise to replace.
     * @param newExerciseInfo The new exercise information.
     */
    public void replaceExerciseRoutine(int position, Exercise newExerciseInfo) {
        Routine currentDay = editableRoutine.getValue();
        if (currentDay != null && currentDay.getWorkoutExercises() != null && position < currentDay.getWorkoutExercises().size()) {

            List<WorkoutExercise> updatedList = new ArrayList<>(currentDay.getWorkoutExercises());

            WorkoutExercise newWorkoutExercise = new WorkoutExercise(newExerciseInfo.getName());
            newWorkoutExercise.setSeries(createDefaultSeries());

            updatedList.set(position, newWorkoutExercise);
            currentDay.setWorkoutExercises(updatedList);

            editableRoutine.setValue(currentDay);
        }
    }

    /**
     * Removes an exercise from the routine at the specified position.
     *
     * @param position The index of the exercise to delete.
     */
    public void deleteExerciseFromRoutine(int position) {
        Routine routine = editableRoutine.getValue();
        if (routine != null && routine.getWorkoutExercises() != null) {
            List<WorkoutExercise> updatedList = new ArrayList<>(routine.getWorkoutExercises());
            if (position >= 0 && position < updatedList.size()) {
                updatedList.remove(position);
                routine.setWorkoutExercises(updatedList);
                editableRoutine.setValue(routine);
            }
        }
    }

    /**
     * Adds a new set to a specific exercise in the routine.
     *
     * @param exercisePosition The index of the exercise.
     */
    public void addSetInExercise(int exercisePosition) {
        Routine routine = editableRoutine.getValue();
        if (routine != null && routine.getWorkoutExercises() != null) {
            if (exercisePosition < routine.getWorkoutExercises().size()) {

                List<WorkoutExercise> updatedExercises = new ArrayList<>(routine.getWorkoutExercises());
                WorkoutExercise workoutExercise = updatedExercises.get(exercisePosition);

                List<Serie> currentSeries = workoutExercise.getSeries();
                if (currentSeries == null) {
                    currentSeries = new ArrayList<>();
                }

                List<Serie> updatedSeries = new ArrayList<>(currentSeries);

                Serie newSet = new Serie();
                newSet.setUserId(routine.getUserId());
                newSet.setWorkoutExerciseId(workoutExercise.getWorkoutExerciseId());

                updatedSeries.add(newSet);
                workoutExercise.setSeries(updatedSeries);

                routine.setWorkoutExercises(updatedExercises);
                editableRoutine.setValue(routine);
            }
        }
    }

    /**
     * Updates the weight and reps for a specific set in an exercise.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @param newWeight        The new weight value.
     * @param newReps          The new repetition count.
     */
    public void updateSetInExercise(int exercisePosition, int setPosition, double newWeight, int newReps) {
        Routine routine = editableRoutine.getValue();
        if (routine != null && routine.getWorkoutExercises() != null) {
            if (exercisePosition < routine.getWorkoutExercises().size()) {

                // Crea una nuova lista di esercizi per forzare l'aggiornamento della UI
                List<WorkoutExercise> updatedExercises = new ArrayList<>(routine.getWorkoutExercises());
                WorkoutExercise workoutExercise = updatedExercises.get(exercisePosition);

                if (workoutExercise.getSeries() != null && setPosition < workoutExercise.getSeries().size()) {
                    Serie serie = workoutExercise.getSeries().get(setPosition);
                    serie.setTargetWeight(newWeight);
                    serie.setTargetReps(newReps);

                    // Aggiorna la lista nella routine e notifica
                    routine.setWorkoutExercises(updatedExercises);
                    editableRoutine.setValue(routine);
                }
            }
        }
    }

    /**
     * Deletes a set from a specific exercise.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set to delete.
     */
    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        Routine routine = editableRoutine.getValue();
        if (routine != null && routine.getWorkoutExercises() != null) {
            if (exercisePosition < routine.getWorkoutExercises().size()) {

                // Crea una nuova lista di esercizi per forzare l'aggiornamento della UI
                List<WorkoutExercise> updatedExercises = new ArrayList<>(routine.getWorkoutExercises());
                WorkoutExercise workoutExercise = updatedExercises.get(exercisePosition);

                if (workoutExercise.getSeries() != null && setPosition < workoutExercise.getSeries().size()) {
                    workoutExercise.getSeries().remove(setPosition);

                    // Aggiorna la lista nella routine e notifica
                    routine.setWorkoutExercises(updatedExercises);
                    editableRoutine.setValue(routine);
                }
            }
        }
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Cleans up resources such as detaching repository listeners to prevent memory leaks.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        trainingRepository.detachTrainingsListener();
    }

    /**
     * Resets the local database by clearing training and exercise data.
     */
    public void resetLocalDatabase() {
        trainingRepository.resetLocalDatabase();
        exerciseRepository.resetLocalDatabase();
    }

    /**
     * Clears the currently editable routine from the ViewModel.
     */
    public void clearEditableRoutine() {
        editableRoutine.setValue(null);
    }
}