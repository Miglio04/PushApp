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

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final LiveData<Result> trainings;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData per la modifica
    private final MutableLiveData<Routine> editableRoutine = new MutableLiveData<>();

    // LiveData per esercizi e filtri
    private final LiveData<Result> availableExercises;
    private final MediatorLiveData<Result> filteredAvailableExercises = new MediatorLiveData<>();

    // LiveData per le Categorie dei Filtri
    private final MutableLiveData<List<String>> availableMuscleGroups = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableDifficulties = new MutableLiveData<>();

    // Cache esercizi
    private List<Exercise> fullExerciseList = new ArrayList<>();

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

    // --- CRUD OPERAZIONI ---
    public void createSampleTraining(String userId) {
        trainingRepository.createSampleTraining(userId);
    }
    public void createTraining(String userId) {
        isLoading.setValue(true);
        Training training = new Training();
        training.setUserId(userId);
        training.setName("New Training");
        training.setDescription("Description");
        trainingRepository.createTraining(userId, training);
    }
    public void fetchTrainings(String userId){
        trainingRepository.fetchTrainings(userId);
    }
    public void updateTraining(Training training) {
        trainingRepository.updateTraining(training);
    }
    public void deleteTraining(Training training) {
        trainingRepository.deleteTraining(training);
    }

    public void createRoutine(Training currentTraining) {
        Routine routine = new Routine();
        routine.setName("New Routine");
        routine.setTrainingId(currentTraining.getTrainingId());
        routine.setUserId(currentTraining.getUserId());
        trainingRepository.createRoutine(routine);
    }
    public void updateRoutine(Routine routine){
        trainingRepository.updateRoutine(routine);
    }
    public void deleteRoutine(Routine routine) {
        trainingRepository.deleteRoutine(routine);
    }


    // --- EDIT MODE LOGIC ---
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


    // ===================================================================================
    // CARICAMENTO ESERCIZI
    // ===================================================================================

    public void loadAvailableExercises() {
        if (!fullExerciseList.isEmpty()) {
            if (filteredAvailableExercises.getValue() == null || (filteredAvailableExercises.getValue().isExerciseSuccess() && ((Result.ExerciseSuccess)filteredAvailableExercises.getValue()).getData().isEmpty())) {
                filteredAvailableExercises.setValue(new Result.ExerciseSuccess(fullExerciseList));
            }
            return;
        }
        exerciseRepository.fetchExercises();
    }

    private void extractFilterCategories(List<Exercise> exercises) {
        Set<String> muscleSet = new HashSet<>();
        Set<String> difficultySet = new HashSet<>();

        for (Exercise exercise : exercises) {
            // Muscoli
            if (exercise.getMuscle() != null && !exercise.getMuscle().isEmpty()) {
                muscleSet.add(capitalize(exercise.getMuscle()));
            }
            // Difficoltà
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
    public void applyFilters(String query, String muscleGroup, String difficultyGroup) {
        List<Exercise> tempFilteredList = new ArrayList<>(fullExerciseList);
        List<Exercise> nextStageList = new ArrayList<>();

        String muscleFilter = (muscleGroup == null) ? "Tutti" : muscleGroup;
        if (muscleFilter.equalsIgnoreCase("Tutti")) {
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

        String diffFilter = (difficultyGroup == null) ? "Tutti" : difficultyGroup;
        if (diffFilter.equalsIgnoreCase("Tutti")) {
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
                if (exercise.getName() != null && exercise.getName().toLowerCase().contains(lowerCaseQuery)) {
                    nextStageList.add(exercise);
                }
            }
            filteredAvailableExercises.setValue(new Result.ExerciseSuccess(nextStageList));
        }
    }
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String formatted = str.replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    // ===================================================================================
    // GESTIONE ESERCIZI NEL GIORNO
    // ===================================================================================

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

    public void replaceExerciseRoutine(int position, Exercise newExerciseInfo) {
        Routine currentDay = editableRoutine.getValue();
        if (currentDay != null && currentDay.getWorkoutExercises() != null && position < currentDay.getWorkoutExercises().size()) {

            List<WorkoutExercise> updatedList = new ArrayList<>(currentDay.getWorkoutExercises());

            WorkoutExercise newWorkoutExercise = new WorkoutExercise(newExerciseInfo.getName(), position + 1);
            newWorkoutExercise.setSeries(createDefaultSeries());

            updatedList.set(position, newWorkoutExercise);
            currentDay.setWorkoutExercises(updatedList);

            editableRoutine.setValue(currentDay);
        }
    }

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

    public void addSetInExercise(int exercisePosition) {
        Routine routine = editableRoutine.getValue();
        if (routine != null && routine.getWorkoutExercises() != null) {
            if (exercisePosition < routine.getWorkoutExercises().size()) {

                // Crea una nuova lista di esercizi per forzare l'aggiornamento della UI
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

                // Aggiorna la lista nella routine e notifica
                routine.setWorkoutExercises(updatedExercises);
                editableRoutine.setValue(routine);
            }
        }
    }
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

    @Override
    protected void onCleared() {
        super.onCleared();
        trainingRepository.detachTrainingsListener();
    }

    public void resetLocalDatabase() {
        trainingRepository.resetLocalDatabase();
        exerciseRepository.resetLocalDatabase();
    }
    public void clearEditableRoutine() {
        editableRoutine.setValue(null);
    }
}
