package com.example.pushapp.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.utils.FirebaseCallback;

import java.util.ArrayList;
import java.util.List;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository repository;
    private final MutableLiveData<List<Training>> trainings = new MutableLiveData<>();
    private final MutableLiveData<Training> activeTraining = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<TrainingDay> editableTrainingDay = new MutableLiveData<>();

    public TrainingViewModel() {
        this.repository = new TrainingRepository();
    }

    public LiveData<List<Training>> getTrainings() { return trainings; }
    public LiveData<Training> getActiveTraining() { return activeTraining; }
    public LiveData<TrainingDay> getEditableTrainingDay() { return editableTrainingDay; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadTrainings() {
        isLoading.setValue(true);
        repository.getUserTrainings(new FirebaseCallback<List<Training>>() {
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
        repository.getActiveTraining(new FirebaseCallback<Training>() {
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
        repository.createTraining(training, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                loadTrainings();
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
        repository.updateTraining(training, callback);
    }

    public void deleteTraining(String trainingId, FirebaseCallback<Void> callback) {
        repository.deleteTraining(trainingId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTrainings();
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void loadTrainingDayForEdit(String trainingId, String trainingDayId) {
        // TODO: In futuro, qui caricherai i dati reali da Firebase usando il repository.
        // Per adesso, usiamo dati di esempio per far funzionare l'interfaccia.
        TrainingDay day = new TrainingDay("Giorno da Modificare (Caricato)", 1);
        day.setId(trainingDayId);

        Exercise bench = new Exercise("Bench Press", "Petto", 1);
        bench.addSerie(new Serie(1, 8, 80));
        bench.addSerie(new Serie(2, 8, 80));
        day.addExercise(bench);

        Exercise squat = new Exercise("Squat", "Gambe", 2);
        squat.addSerie(new Serie(1, 5, 100));
        day.addExercise(squat);

        editableTrainingDay.setValue(day);
    }

    public void addExerciseToDay(Exercise exercise) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null) {
            currentDay.addExercise(exercise);
            // Notifica l'observer che i dati sono cambiati
            editableTrainingDay.setValue(currentDay);
        }
    }

    public void replaceExerciseInDay(int position, Exercise newExercise) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null) {
            List<Exercise> exercises = currentDay.getExercises();
            if (position >= 0 && position < exercises.size()) {
                // Manteniamo le serie dell'esercizio vecchio, se presenti, o le resettiamo.
                // Per ora, le resettiamo per semplicità.
                newExercise.setSeries(new ArrayList<>());
                newExercise.addSerie(new Serie(1, 8, 40)); // Aggiungiamo una serie di default

                // Sostituisce l'esercizio nella lista
                exercises.set(position, newExercise);

                // Notifica l'observer che i dati sono cambiati
                editableTrainingDay.setValue(currentDay);
            }
        }
    }

    public void deleteExerciseFromDay(int position) {
        TrainingDay currentDay = editableTrainingDay.getValue();
        if (currentDay != null && currentDay.getExercises() != null && position >= 0 && position < currentDay.getExercises().size()) {
            currentDay.getExercises().remove(position);
            // Notifica l'observer
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
                    // Notifica l'observer che i dati sono cambiati
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
                    // Notifica l'observer
                    editableTrainingDay.setValue(currentDay);
                }
            }
        }
    }

}
