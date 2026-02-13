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
        ServiceLocator sl = serviceLocator;

        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            // UserViewModel requires (UserRepository, SessionRepository)
            return (T) new UserViewModel(sl.getUserRepository(context), sl.getSessionRepository(context));
        } else if (modelClass.isAssignableFrom(TrainingViewModel.class)) {
            // TrainingViewModel requires (TrainingRepository, ExerciseRepository)
            return (T) new TrainingViewModel(sl.getTrainingRepository(context), sl.getExerciseRepository(context));
        } else if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            // HistoryViewModel requires (HistoryRepository)
            return (T) new HistoryViewModel(sl.getHistoryRepository(context));
        } else if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
            // WorkoutViewModel requires (ExerciseRepository, HistoryRepository, SessionManager)
            return (T) new WorkoutViewModel(
                    sl.getExerciseRepository(context),
                    sl.getHistoryRepository(context),
                    sl.getSessionManager(context)
            );
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
