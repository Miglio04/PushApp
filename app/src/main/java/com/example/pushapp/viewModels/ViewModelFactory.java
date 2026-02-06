package com.example.pushapp.viewModels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.HistoryRepository; // --- NUOVO IMPORT
import com.example.pushapp.repositories.ServiceLocator;
import com.example.pushapp.repositories.SessionRepository;
import com.example.pushapp.repositories.TrainingRepository;
import com.example.pushapp.repositories.UserRepository;
import com.example.pushapp.utils.SessionManager; // --- NUOVO IMPORT

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
            // 1. Recuperiamo le dipendenze esistenti
            TrainingRepository trainingRepository = ServiceLocator.getInstance().getTrainingRepository(context);
            ExerciseRepository exerciseRepository = ServiceLocator.getInstance().getExerciseRepository(context);

            // 2. --- NUOVO --- Recuperiamo le nuove dipendenze per Storico e Anti-Crash
            HistoryRepository historyRepository = ServiceLocator.getInstance().getHistoryRepository(context);
            SessionManager sessionManager = ServiceLocator.getInstance().getSessionManager(context);

            // 3. --- NUOVO --- Passiamo tutti e 4 i parametri al costruttore
            return (T) new WorkoutViewModel(trainingRepository, exerciseRepository, historyRepository, sessionManager);

        } else if (modelClass.isAssignableFrom(UserViewModel.class)) {
            UserRepository userRepository = ServiceLocator.getInstance().getUserRepository(context);
            SessionRepository sessionRepository = ServiceLocator.getInstance().getSessionRepository(context);
            return (T) new UserViewModel(userRepository, sessionRepository);
        }

        // --- NUOVO: Aggiungi anche HistoryViewModel qui sotto (ti servirà tra poco per lo storico) ---
        // else if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
        //    HistoryRepository historyRepository = ServiceLocator.getInstance().getHistoryRepository(context);
        //    return (T) new HistoryViewModel(historyRepository);
        // }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}