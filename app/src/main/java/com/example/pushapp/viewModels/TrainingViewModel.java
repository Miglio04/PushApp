package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.ExerciseApiModel;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.repositories.FirebaseCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final LiveData<Result> trainings;
    private final MutableLiveData<Training> activeTraining = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData per la modifica
    private final MutableLiveData<TrainingDay> editableTrainingDay = new MutableLiveData<>();

    // LiveData per esercizi e filtri
    private final MutableLiveData<List<ExerciseApiModel>> availableExercises = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseApiModel>> filteredAvailableExercises = new MutableLiveData<>();

    // LiveData per le Categorie dei Filtri (Rimosso Equipment)
    private final MutableLiveData<List<String>> availableMuscleGroups = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableDifficulties = new MutableLiveData<>();

    // Cache esercizi
    private List<ExerciseApiModel> fullExerciseList = new ArrayList<>();

    private boolean isListenerAttached = false;

    public TrainingViewModel(TrainingRepository trainingRepository, ExerciseRepository exerciseRepository){
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.trainings = trainingRepository.getTrainingList();
    }

    public LiveData<Result> getTrainings() { return trainings; }
    public LiveData<Training> getActiveTraining() { return activeTraining; }
    public LiveData<TrainingDay> getEditableTrainingDay() { return editableTrainingDay; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<List<ExerciseApiModel>> getAvailableExercises() { return availableExercises; }
    public LiveData<List<ExerciseApiModel>> getFilteredAvailableExercises() { return filteredAvailableExercises; }

    public LiveData<List<String>> getAvailableMuscleGroups() { return availableMuscleGroups; }
    public LiveData<List<String>> getAvailableDifficulties() { return availableDifficulties; }


    public void fetchTrainings(){
        newFetchTrainings();
    }

    public void oldFetchTrainings() {
        if (isListenerAttached) {
            return; // Evita di attaccare listener multipli
        }

        isLoading.setValue(true);
        isListenerAttached = true;

        trainingRepository.attachUserTrainingsListener(new FirebaseCallback<List<Training>>() {
            @Override
            public void onSuccess(List<Training> result) {
                //trainings.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void newFetchTrainings(){
        trainingRepository.getTrainingList();
    }


    public void loadActiveTraining() {
        trainingRepository.getActiveTraining(new FirebaseCallback<Training>() {
            @Override
            public void onSuccess(Training result) {
                activeTraining.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    // --- CRUD OPERAZIONI ---
    public void createTraining(Training training, FirebaseCallback<String> callback) {
        isLoading.setValue(true);
        trainingRepository.createTraining(training, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                isLoading.setValue(false);
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
                isLoading.setValue(false);
                callback.onError(e);
            }
        });
    }

    public void updateTraining(Training training, FirebaseCallback<Void> callback) {
        trainingRepository.updateTraining(training);
    }

    public void deleteTraining(String trainingId, FirebaseCallback<Void> callback) {
        trainingRepository.deleteTraining(trainingId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // --- EDIT MODE LOGIC ---
    public void loadTrainingDayForEdit(String trainingId, String trainingDayId) {
        isLoading.setValue(true);

        if (trainingId == null || trainingDayId == null) {
            errorMessage.setValue("ID mancante per il caricamento.");
            isLoading.setValue(false);
            return;
        }

        // Cerca il training corretto nella lista già caricata
        if (trainings.getValue().isTrainingsSuccess()) {
            List<Training> currentTrainings = ((Result.TrainingsSuccess) trainings.getValue()).getData();
            ;
            if (currentTrainings != null && trainingId != null) {
                for (Training t : currentTrainings) {
                    if (trainingId.equals(t.getTrainingId()) && t.getTrainingDaysList() != null) {
                        // Trovato il training, ora cerca il giorno
                        for (TrainingDay day : t.getTrainingDaysList()) {
                            if (trainingDayId.equals(day.getTrainingDayId())) {
                                editableTrainingDay.setValue(day); // Pubblica il giorno reale
                                isLoading.setValue(false);
                                return;
                            }
                            boolean foundInCache = false;

                            if (currentTrainings != null && !currentTrainings.isEmpty()) {
                                foundInCache = attemptToFindAndSetDay(currentTrainings, trainingId, trainingDayId);
                            }

                            /*if (!foundInCache) {
                                trainingRepository.attachUserTrainingsListener(new FirebaseCallback<List<Training>>() {
                                    @Override
                                    public void onSuccess(List<Training> result) {
                                        trainings.setValue(new Result.TrainingsSuccess(result));
                                        boolean foundAfterFetch = attemptToFindAndSetDay(result, trainingId, trainingDayId);
                                        if (!foundAfterFetch) {
                                            errorMessage.setValue("Giorno non trovato nemmeno dopo il caricamento.");
                                            isLoading.setValue(false);
                                        }
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        errorMessage.setValue("Impossibile scaricare i dati: " + e.getMessage());
                                        isLoading.setValue(false);
                                    }
                                });
                            }*/
                        }
                    }
                }
            }
        }
    }

    private boolean attemptToFindAndSetDay(List<Training> trainingList, String trainingId, String trainingDayId) {
        for (Training t : trainingList) {
            if (t.getTrainingId() != null && t.getTrainingId().trim().equals(trainingId.trim())) {
                if (t.getTrainingDaysList() != null) {
                    for (TrainingDay day : t.getTrainingDaysList()) {
                        if (day.getTrainingId() != null && day.getTrainingId().trim().equals(trainingDayId.trim())) {
                            editableTrainingDay.setValue(day);
                            isLoading.setValue(false);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void saveTrainingDayChanges(String trainingId, FirebaseCallback<Void> callback) {
        TrainingDay editedDay = editableTrainingDay.getValue();
        if (trainings.getValue().isTrainingsSuccess()) {
            List<Training> currentTrainings = ((Result.TrainingsSuccess) trainings.getValue()).getData();

            if (editedDay == null || currentTrainings == null || trainingId == null) {
                callback.onError(new Exception("Dati mancanti per il salvataggio"));
            }

            for (Training training : currentTrainings) {
                if (trainingId.equals(training.getTrainingId())) {
                    List<TrainingDay> days = training.getTrainingDaysList();
                    if (days != null) {
                        for (int i = 0; i < days.size(); i++) {
                            if (editedDay.getTrainingId().equals(days.get(i).getTrainingId())) {
                                days.set(i, editedDay);
                                break;
                            }
                        }
                        // Salva il training aggiornato su Firebase
                        trainingRepository.updateTraining(training);
                    }
                }
            }
        }
    }

    // ===================================================================================
    // CARICAMENTO ESERCIZI
    // ===================================================================================

    public void loadAvailableExercises() {
        if (!fullExerciseList.isEmpty()) {
            if (availableExercises.getValue() == null) {
                availableExercises.setValue(fullExerciseList);
            }
            return;
        }

        exerciseRepository.getAvailableExercises(new FirebaseCallback<List<ExerciseApiModel>>() {
            @Override
            public void onSuccess(List<ExerciseApiModel> result) {
                fullExerciseList = result;
                availableExercises.setValue(result);
                filteredAvailableExercises.setValue(fullExerciseList);
                extractFilterCategories(result);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Errore caricamento esercizi: " + e.getMessage());
            }
        });
    }

    private void extractFilterCategories(List<ExerciseApiModel> exercises) {
        Set<String> muscleSet = new HashSet<>();
        Set<String> difficultySet = new HashSet<>();

        for (ExerciseApiModel exercise : exercises) {
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

        // ORDINAMENTO PERSONALIZZATO DIFFICOLTÀ: Beginner -> Intermediate -> Expert
        List<String> sortedDifficulties = new ArrayList<>(difficultySet);
        final List<String> order = Arrays.asList("Beginner", "Intermediate", "Expert");

        Collections.sort(sortedDifficulties, new Comparator<String>() {
            @Override
            public int compare(String d1, String d2) {
                int i1 = order.indexOf(d1);
                int i2 = order.indexOf(d2);

                // Se entrambi sono nella lista, usa l'ordine definito
                if (i1 != -1 && i2 != -1) return Integer.compare(i1, i2);

                // Se uno non è nella lista, mettilo in fondo
                if (i1 == -1 && i2 == -1) return d1.compareToIgnoreCase(d2); // Fallback alfabetico
                if (i1 == -1) return 1;
                if (i2 == -1) return -1;
                return 0;
            }
        });

        availableDifficulties.setValue(sortedDifficulties);
    }

    // --- LOGICA DI FILTRO (3 ARGOMENTI: Query, Muscolo, Difficoltà) ---
    public void applyFilters(String query, String muscleGroup, String difficultyGroup) {
        List<ExerciseApiModel> tempFilteredList = new ArrayList<>(fullExerciseList);
        List<ExerciseApiModel> nextStageList = new ArrayList<>();

        // 1. FILTRO MUSCOLI
        String muscleFilter = (muscleGroup == null) ? "Tutti" : muscleGroup;
        if (muscleFilter.equalsIgnoreCase("Tutti")) {
            nextStageList.addAll(tempFilteredList);
        } else {
            for (ExerciseApiModel exercise : tempFilteredList) {
                if (exercise.getMuscle() != null && capitalize(exercise.getMuscle()).equalsIgnoreCase(muscleFilter)) {
                    nextStageList.add(exercise);
                }
            }
        }
        tempFilteredList = new ArrayList<>(nextStageList);
        nextStageList.clear();

        // 2. FILTRO DIFFICOLTÀ
        String diffFilter = (difficultyGroup == null) ? "Tutti" : difficultyGroup;
        if (diffFilter.equalsIgnoreCase("Tutti")) {
            nextStageList.addAll(tempFilteredList);
        } else {
            for (ExerciseApiModel exercise : tempFilteredList) {
                if (exercise.getDifficulty() != null && capitalize(exercise.getDifficulty()).equalsIgnoreCase(diffFilter)) {
                    nextStageList.add(exercise);
                }
            }
        }
        tempFilteredList = new ArrayList<>(nextStageList);
        nextStageList.clear();

        // 3. FILTRO RICERCA TESTUALE
        if (query == null || query.isEmpty()) {
            filteredAvailableExercises.setValue(tempFilteredList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (ExerciseApiModel exercise : tempFilteredList) {
                if (exercise.getName() != null && exercise.getName().toLowerCase().contains(lowerCaseQuery)) {
                    nextStageList.add(exercise);
                }
            }
            filteredAvailableExercises.setValue(nextStageList);
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
            defaultSet.setTargetWeight(0.0);
            defaultSet.setTargetReps(0);
            series.add(defaultSet);
        }
        return series;
    }

    public void addExerciseToDay(Exercise exercise) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null) {
            List<Exercise> currentList = currentDay.getExercises();
            if (currentList == null) currentList = new ArrayList<>();

            List<Exercise> updatedList = new ArrayList<>(currentList);

            if (exercise.getSeries() == null || exercise.getSeries().isEmpty()) {
                exercise.setSeries(createDefaultSeries());
            }

            updatedList.add(exercise);
            currentDay.setExercises(updatedList);
            editableTrainingDay.setValue(currentDay);
        } else {
            errorMessage.setValue("Errore: Giorno non caricato.");
        }
    }

    public void replaceExerciseInDay(int position, ExerciseApiModel newExerciseInfo) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null && position < currentDay.getExercises().size()) {

            List<Exercise> updatedList = new ArrayList<>(currentDay.getExercises());

            Exercise newExercise = new Exercise(newExerciseInfo.getName().hashCode(), newExerciseInfo.getName(), position + 1);
            newExercise.setSeries(createDefaultSeries());

            updatedList.set(position, newExercise);
            currentDay.setExercises(updatedList);

            editableTrainingDay.setValue(currentDay);
        }
    }

    public void deleteExerciseFromDay(int position) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null) {
            List<Exercise> updatedList = new ArrayList<>(currentDay.getExercises());
            if (position >= 0 && position < updatedList.size()) {
                updatedList.remove(position);
                currentDay.setExercises(updatedList);
                editableTrainingDay.setValue(currentDay);
            }
        }
    }

    public void updateSetInExercise(int exercisePosition, int setPosition, double newWeight, int newReps) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null) {
            if (exercisePosition < currentDay.getExercises().size()) {
                Exercise exercise = currentDay.getExercises().get(exercisePosition);
                if (exercise.getSeries() != null && setPosition < exercise.getSeries().size()) {
                    Serie serie = exercise.getSeries().get(setPosition);
                    serie.setTargetWeight(newWeight);
                    serie.setTargetReps(newReps);
                    editableTrainingDay.setValue(currentDay);
                }
            }
        }
    }

    public void deleteSetFromExercise(int exercisePosition, int setPosition) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null) {
            if (exercisePosition < currentDay.getExercises().size()) {
                Exercise exercise = currentDay.getExercises().get(exercisePosition);
                if (exercise.getSeries() != null && setPosition < exercise.getSeries().size()) {
                    exercise.getSeries().remove(setPosition);
                    editableTrainingDay.setValue(currentDay);
                }
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        trainingRepository.detachTrainingsListener();
        isListenerAttached = false;
    }
}
