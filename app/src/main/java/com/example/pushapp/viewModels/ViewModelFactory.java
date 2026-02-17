package com.example.pushapp.viewModels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.pushapp.repositories.ServiceLocator;

/**
 * Factory class for creating ViewModels with dependencies.
 * Ensures the correct repositories and services are injected into ViewModels.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final ServiceLocator serviceLocator;
    private HistoryViewModel historyViewModelInstance;

    /**
     * Constructs a ViewModelFactory with the application context.
     *
     * @param context The application context used to retrieve repositories.
     */
    public ViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
        this.serviceLocator = ServiceLocator.getInstance();
    }

    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @param <T>        The type of the ViewModel.
     * @return A newly created ViewModel instance with injected dependencies.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
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
            if (historyViewModelInstance == null) {
                historyViewModelInstance = new HistoryViewModel(locator.getHistoryRepository(context));
            }
            return (T) historyViewModelInstance;

        } else if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
            HistoryViewModel sharedHistoryViewModel = create(HistoryViewModel.class);

            return (T) new WorkoutViewModel(
                    locator.getExerciseRepository(context),
                    sharedHistoryViewModel,
                    locator.getSessionManager(context)
            );
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
