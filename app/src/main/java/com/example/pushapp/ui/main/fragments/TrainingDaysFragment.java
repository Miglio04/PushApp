// Sostituisci l'intero contenuto di TrainingDaysFragment.java con questo
package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay; // <-- Import corretto
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.TrainingDaysCard;
import com.example.pushapp.utils.TrainingDaysCardAdapter;
import com.example.pushapp.utils.TrainingViewModel; // <-- USA IL VIEWMODEL CORRETTO
import com.example.pushapp.utils.WorkoutViewModel;

import java.io.Serializable; // <-- Aggiungi import per il passaggio dati
import java.util.ArrayList;
import java.util.List;

public class TrainingDaysFragment extends Fragment {

    private String trainingId;
    private TrainingViewModel trainingViewModel; // <-- USA IL VIEWMODEL CORRETTO
    private WorkoutViewModel workoutViewModel;
    private TrainingDaysCardAdapter adapter;
    private Training currentTraining; // Campo per memorizzare il training corrente

    public TrainingDaysFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inizializza ENTRAMBI i ViewModel
        trainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        if (getArguments() != null) {
            trainingId = getArguments().getString("trainingId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training_days, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_training_days);
        setupRecyclerView(recyclerView);
        observeViewModel();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Crea l'adapter una sola volta con una lista vuota
        adapter = new TrainingDaysCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Imposta i listener sull'adapter
        adapter.setStartWorkoutListener(this::handleStartWorkoutClick);
        adapter.setEditWorkoutListener(this::handleEditDayClick);
    }

    private void observeViewModel() {
        // Osserva la lista completa di allenamenti
        trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings == null || trainingId == null) return;

            // Cerca il training specifico che ci interessa usando l'ID (String)
            for (Training training : trainings) {
                if (trainingId.equals(training.getId())) {
                    currentTraining = training; // Salva il training trovato
                    // Genera le card usando i dati REALI dal training trovato
                    List<TrainingDaysCard> cards = generateCardsFromTraining(currentTraining);
                    adapter.updateCards(cards); // Aggiorna l'adapter con le nuove card
                    break;
                }
            }
        });
    }

    // --- NUOVI METODI HELPER ---

    private List<TrainingDaysCard> generateCardsFromTraining(Training training) {
        List<TrainingDaysCard> cards = new ArrayList<>();
        if (training == null || training.getTrainingDaysList() == null) {
            return cards;
        }

        // Crea una card per ogni giorno di allenamento reale
        for (TrainingDay day : training.getTrainingDaysList()) {
            cards.add(new TrainingDaysCard(day.getName(), "Exercises: " + day.getTotalExercises(), day.getId()));
        }
        return cards;
    }

    private void handleStartWorkoutClick(TrainingDaysCard card) {
        Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();
        if (Boolean.TRUE.equals(isWorkoutInProgress)) {
            showReplaceWorkoutDialog(card);
        } else {
            startNewWorkout(card);
        }
    }

    private void handleEditDayClick(TrainingDaysCard card) {
        if (getView() != null && card.getTrainingDayId() != null) {
            NavController navController = Navigation.findNavController(getView());
            Bundle args = new Bundle();
            args.putString("trainingId", trainingId);
            args.putString("trainingDayId", card.getTrainingDayId());
            navController.navigate(R.id.nav_training_days_to_edit, args);
        } else {
            // Opzionale: mostra un messaggio di errore
            Toast.makeText(getContext(), "Errore: ID del giorno non disponibile", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewWorkout(TrainingDaysCard card) {
        if (currentTraining == null) return;

        String cardDayId = card.getTrainingDayId();
        if (cardDayId == null) return; // Aggiungi questo check

        // Trova il TrainingDay completo da passare al WorkoutFragment
        for (TrainingDay day : currentTraining.getTrainingDaysList()) {
            if (cardDayId.equals(day.getId())) { // Inverti il confronto
                NavController navController = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", (Serializable) day);
                args.putSerializable("parentTraining", (Serializable) currentTraining);
                navController.navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    private void showReplaceWorkoutDialog(TrainingDaysCard card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout in corso")
                .setMessage("Hai già un workout in corso. Vuoi scartarlo e avviarne uno nuovo?")
                .setPositiveButton("Scarta e avvia", (dialog, which) -> {
                    workoutViewModel.stopWorkout(new FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            startNewWorkout(card);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Avvia comunque il nuovo workout anche se il salvataggio fallisce
                            startNewWorkout(card);
                        }
                    });
                })
                .setNegativeButton("Annulla", null)
                .show();
    }
}
