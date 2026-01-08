package com.example.pushapp.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;

public class WorkoutViewModelFactory implements ViewModelProvider.Factory {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutViewModelFactory(TrainingRepository trainingRepository, ExerciseRepository exerciseRepository) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new WorkoutViewModel(trainingRepository, exerciseRepository);
    }
}