package com.example.pushapp.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;

public class TrainingViewModelFactory implements ViewModelProvider.Factory {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

    public TrainingViewModelFactory(TrainingRepository trainingRepository, ExerciseRepository exerciseRepository) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TrainingViewModel(trainingRepository, exerciseRepository);
    }
}