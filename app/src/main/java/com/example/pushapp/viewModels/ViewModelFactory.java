package com.example.pushapp.viewModels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.utils.ServiceLocator;
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.repositories.UserRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public ViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(TrainingViewModel.class)) {
            TrainingRepository trainingRepository = ServiceLocator.getInstance().getTrainingRepository(context);
            ExerciseRepository exerciseRepository = ServiceLocator.getInstance().getExerciseRepository(context);
            return (T) new TrainingViewModel(trainingRepository, exerciseRepository);

        } else if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
                TrainingRepository trainingRepository = ServiceLocator.getInstance().getTrainingRepository(context);
                ExerciseRepository exerciseRepository = ServiceLocator.getInstance().getExerciseRepository(context);
                return (T) new WorkoutViewModel(trainingRepository, exerciseRepository);

            } else if (modelClass.isAssignableFrom(UserViewModel.class)) {
            UserRepository userRepository = ServiceLocator.getInstance().getUserRepository(context);
            return (T) new UserViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
