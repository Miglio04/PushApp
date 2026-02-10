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
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine; // <-- Import corretto
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.ui.main.graphicComponents.RoutinesCard;
import com.example.pushapp.adapter.RoutineCardAdapter;
import com.example.pushapp.viewModels.TrainingViewModel; // <-- USA IL VIEWMODEL CORRETTO
import com.example.pushapp.viewModels.WorkoutViewModel;

import java.io.Serializable; // <-- Aggiungi import per il passaggio dati
import java.util.ArrayList;
import java.util.List;

public class RoutineFragment extends Fragment {

    private String trainingId;
    private TrainingViewModel trainingViewModel; // <-- USA IL VIEWMODEL CORRETTO
    private WorkoutViewModel workoutViewModel;
    private RoutineCardAdapter adapter;
    private Training currentTraining; // Campo per memorizzare il training corrente

    public RoutineFragment() { }

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
        return inflater.inflate(R.layout.fragment_routines, container, false);
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
        adapter = new RoutineCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Imposta i listener sull'adapter
        adapter.setStartWorkoutListener(this::handleStartWorkoutClick);
        adapter.setEditWorkoutListener(this::handleEditDayClick);
    }

    private void observeViewModel() {
        trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings == null) {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }else if(!trainings.isTrainingsSuccess()){
                Toast.makeText(getContext(), ((Result.Error) trainings).getMessage(), Toast.LENGTH_LONG).show();
            }else{
                List<Training> trainingsList = ((Result.TrainingsSuccess) trainings).getData();
                for (Training training : trainingsList) {
                    if (trainingId.equals(training.getTrainingId())) {
                        currentTraining = training;
                        List<RoutinesCard> cards = generateCardsFromTraining(currentTraining);
                        adapter.updateCards(cards);
                        break;
                    }
                }
            }
        });
    }

    // --- NUOVI METODI HELPER ---

    private List<RoutinesCard> generateCardsFromTraining(Training training) {
        List<RoutinesCard> cards = new ArrayList<>();
        if (training == null || training.getRoutinesList() == null) {
            return cards;
        }

        // Crea una card per ogni giorno di allenamento reale
        for (Routine day : training.getRoutinesList()) {
            cards.add(new RoutinesCard(day.getName(), "Exercises: " + day.getWorkoutTotalExercises(), day.getRoutineId()));
        }
        return cards;
    }

    private void handleStartWorkoutClick(RoutinesCard card) {
        Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();
        if (Boolean.TRUE.equals(isWorkoutInProgress)) {
            showReplaceWorkoutDialog(card);
        } else {
            startNewWorkout(card);
        }
    }

    private void handleEditDayClick(RoutinesCard card) {
        if (getView() != null && card.getTrainingDayId() != null) {
            NavController navController = Navigation.findNavController(getView());
            Bundle args = new Bundle();

            args.putString("trainingId", trainingId);

            args.putString("dayId", card.getTrainingDayId());

            navController.navigate(R.id.nav_training_days_to_edit, args);
        } else {
            Toast.makeText(getContext(), "Errore: ID del giorno non disponibile", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewWorkout(RoutinesCard card) {
        if (currentTraining == null) return;

        String cardDayId = card.getTrainingDayId();
        if (cardDayId == null) return; // Aggiungi questo check

        // Trova il TrainingDay completo da passare al WorkoutFragment
        for (Routine day : currentTraining.getRoutinesList()) {
            if (cardDayId.equals(day.getRoutineId())) { // Inverti il confronto
                NavController navController = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", (Serializable) day);
                args.putSerializable("parentTraining", (Serializable) currentTraining);
                navController.navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    private void showReplaceWorkoutDialog(RoutinesCard card) {
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
