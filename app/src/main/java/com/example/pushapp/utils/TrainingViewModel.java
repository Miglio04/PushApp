package com.example.pushapp.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.ExerciseApiModel;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.repositories.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final MutableLiveData<List<Training>> trainings = new MutableLiveData<>();
    private final MutableLiveData<Training> activeTraining = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<TrainingDay> editableTrainingDay = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseApiModel>> availableExercises = new MutableLiveData<>();

    private boolean isListenerAttached = false;
    private boolean isExercisesLoaded = false;

    public TrainingViewModel() {
        this.trainingRepository = new TrainingRepository();
        this.exerciseRepository = new ExerciseRepository();
    }

    public LiveData<List<Training>> getTrainings() { return trainings; }
    public LiveData<Training> getActiveTraining() { return activeTraining; }
    public LiveData<TrainingDay> getEditableTrainingDay() { return editableTrainingDay; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<List<ExerciseApiModel>> getAvailableExercises() { return availableExercises; }

    public void loadTrainings() {
        if (isListenerAttached) {
            return; // Evita di attaccare listener multipli
        }

        isLoading.setValue(true);
        isListenerAttached = true;

        trainingRepository.attachUserTrainingsListener(new FirebaseCallback<List<Training>>() {
            @Override
            public void onSuccess(List<Training> result) {
                trainings.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
                isLoading.setValue(false);
            }
        });
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
        trainingRepository.updateTraining(training, callback);
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

    public void loadTrainingDayForEdit(String trainingId, String trainingDayId) {
        isLoading.setValue(true);

        if (trainingId == null || trainingDayId == null) {
            errorMessage.setValue("Training ID or Day ID is null.");
            isLoading.setValue(false);
            return;
        }

        // Cerca il training corretto nella lista già caricata
        List<Training> currentTrainings = trainings.getValue();
        if (currentTrainings != null && trainingId != null) {
            for (Training t : currentTrainings) {
                if (trainingId.equals(t.getId()) && t.getTrainingDaysList() != null) {
                    // Trovato il training, ora cerca il giorno
                    for (TrainingDay day : t.getTrainingDaysList()) {
                        if (trainingDayId.equals(day.getId())) {
                            editableTrainingDay.setValue(day); // Pubblica il giorno reale
                            isLoading.setValue(false);
                            return;
                        }
                    }
                }
            }
        }
        errorMessage.setValue("Training day not found.");
        isLoading.setValue(false);
    }

    public void saveTrainingDayChanges(String trainingId, FirebaseCallback<Void> callback) {
        TrainingDay editedDay = editableTrainingDay.getValue();
        List<Training> currentTrainings = trainings.getValue();

        if (editedDay == null || currentTrainings == null || trainingId == null) {
            callback.onError(new Exception("Dati mancanti per il salvataggio"));
            return;
        }

        // Trova il training e aggiorna il giorno modificato
        for (Training training : currentTrainings) {
            if (trainingId.equals(training.getId())) {
                List<TrainingDay> days = training.getTrainingDaysList();
                if (days != null) {
                    for (int i = 0; i < days.size(); i++) {
                        if (editedDay.getId().equals(days.get(i).getId())) {
                            days.set(i, editedDay); // Sostituisce con il giorno modificato
                            break;
                        }
                    }
                }
                // Salva il training aggiornato su Firebase
                trainingRepository.updateTraining(training, callback);
                return;
            }
        }

        callback.onError(new Exception("Training non trovato"));
    }


    public void loadAvailableExercises() {
        if (isExercisesLoaded || (availableExercises.getValue() != null && !availableExercises.getValue().isEmpty())) {
            return; // API già chiamata o dati già presenti
        }

        isLoading.setValue(true);
        isExercisesLoaded = true; // Impostiamo a true per evitare chiamate concorrenti

        exerciseRepository.getAvailableExercises(new FirebaseCallback<List<ExerciseApiModel>>() {
            @Override
            public void onSuccess(List<ExerciseApiModel> result) {
                availableExercises.setValue(result);
                isLoading.setValue(false);
            }
            @Override
            public void onError(Exception e) {
                isExercisesLoaded = false; // Reset in caso di errore per riprovare
                errorMessage.setValue("Failed to load exercises from API: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void addExerciseToDay(Exercise exercise) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null) {
            currentDay.addExercise(exercise);
            editableTrainingDay.setValue(currentDay);
        }
    }

    public void replaceExerciseInDay(int position, ExerciseApiModel newExerciseInfo) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null && position < currentDay.getExercises().size()) {
            List<Exercise> exercises = currentDay.getExercises();

            Exercise newExercise = new Exercise(newExerciseInfo.getName().hashCode(), newExerciseInfo.getName(), position + 1);
            newExercise.setSeries(new ArrayList<>()); // Inizializza con serie vuote

            exercises.set(position, newExercise);
            editableTrainingDay.setValue(currentDay);
        }
    }

    public void deleteExerciseFromDay(int position) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null && position >= 0 && position < currentDay.getExercises().size()) {
            currentDay.getExercises().remove(position);
            editableTrainingDay.setValue(currentDay);
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
