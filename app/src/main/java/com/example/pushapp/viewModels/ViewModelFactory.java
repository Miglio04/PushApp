package com.example.pushapp.viewModels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.pushapp.repositories.ServiceLocator;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final ServiceLocator serviceLocator;

    public ViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
        this.serviceLocator = ServiceLocator.getInstance();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ServiceLocator locator = serviceLocator;

        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(
                    locator.getUserRepository(context),
                    locator.getSessionRepository(context));

        } else if (modelClass.isAssignableFrom(TrainingViewModel.class)) {
            return (T) new TrainingViewModel(
                    locator.getTrainingRepository(context),
                    locator.getExerciseRepository(context));

        } else if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(
                    locator.getHistoryRepository(context));

        } else if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
            return (T) new WorkoutViewModel(
                    locator.getExerciseRepository(context),
                    locator.getHistoryRepository(context),
                    locator.getSessionManager(context)
            );
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
