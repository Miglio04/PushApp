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
import com.example.pushapp.models.Routine;
import com.example.pushapp.ui.main.graphicComponents.RoutinesCard;
import com.example.pushapp.adapter.RoutineCardAdapter;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.ViewModelFactory; // Assicurati di usare la Factory!
import com.example.pushapp.viewModels.WorkoutViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutineFragment extends Fragment {

    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private RoutineCardAdapter adapter;
    private Training currentTraining;

    public RoutineFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- IMPORTANTE: Usiamo la Factory anche qui ---
        // Se non usi la factory, il WorkoutViewModel non riceverà il SessionManager e crasherà.
        ViewModelFactory factory = new ViewModelFactory(requireContext());

        trainingViewModel = new ViewModelProvider(requireActivity(), factory).get(TrainingViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);

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
        adapter = new RoutineCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

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

    private List<RoutinesCard> generateCardsFromTraining(Training training) {
        List<RoutinesCard> cards = new ArrayList<>();
        if (training == null || training.getRoutinesList() == null) {
            return cards;
        }

        for (Routine day : training.getRoutinesList()) {
            cards.add(new RoutinesCard(day.getName(), "Exercises: " + day.getWorkoutTotalExercises(), day.getRoutineId()));
        }
        return cards;
    }

    private void handleStartWorkoutClick(RoutinesCard card) {
        // Controlliamo se c'è un workout attivo
        Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();

        if (Boolean.TRUE.equals(isWorkoutInProgress)) {
            // Se c'è, chiediamo all'utente cosa fare
            showReplaceWorkoutDialog(card);
        } else {
            // Se non c'è, partiamo diretti
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
        if (cardDayId == null) return;

        for (Routine day : currentTraining.getRoutinesList()) {
            if (cardDayId.equals(day.getRoutineId())) {
                NavController navController = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", (Serializable) day);
                args.putSerializable("parentTraining", (Serializable) currentTraining);
                navController.navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    // --- MODIFICATO: Usa cancelWorkout() ---
    private void showReplaceWorkoutDialog(RoutinesCard card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout in corso")
                .setMessage("Hai già un workout in corso. Vuoi scartarlo e avviarne uno nuovo?")
                .setPositiveButton("Scarta e avvia", (dialog, which) -> {

                    // 1. Cancelliamo il vecchio workout e puliamo la memoria (Anti-Crash)
                    workoutViewModel.cancelWorkout();

                    // 2. Avviamo quello nuovo immediatamente (non serve callback)
                    startNewWorkout(card);

                })
                .setNegativeButton("Annulla", null)
                .show();
    }
}