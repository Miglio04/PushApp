package com.example.pushapp.viewModels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.ServiceLocator;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public ViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ServiceLocator locator = ServiceLocator.getInstance();

        if (modelClass.isAssignableFrom(TrainingViewModel.class)) {
            return (T) new TrainingViewModel(
                    locator.getTrainingRepository(context),
                    locator.getExerciseRepository(context)
            );

        } else if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
            // AGGIORNATO: Ora passiamo 4 parametri al costruttore
            return (T) new WorkoutViewModel(
                    locator.getTrainingRepository(context),
                    locator.getExerciseRepository(context),
                    locator.getHistoryRepository(context), // Per salvare lo storico
                    locator.getSessionManager(context)     // Per gestire i crash/ripristino
            );

        } else if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(
                    locator.getUserRepository(context),
                    locator.getSessionRepository(context)
            );

        } else if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(locator.getHistoryRepository(context));
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}