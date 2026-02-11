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
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.ui.main.graphicComponents.RoutinesCard;
import com.example.pushapp.adapter.RoutineCardAdapter;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
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

        ViewModelFactory factory = new ViewModelFactory(requireContext());
        trainingViewModel = new ViewModelProvider(requireActivity(), factory).get(TrainingViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);

        if (getArguments() != null) {
            trainingId = getArguments().getString("trainingId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            if (trainings instanceof Result.TrainingsSuccess) {
                List<Training> trainingsList = ((Result.TrainingsSuccess) trainings).getData();
                for (Training training : trainingsList) {
                    if (trainingId.equals(training.getTrainingId())) {
                        currentTraining = training;
                        adapter.updateCards(generateCardsFromTraining(currentTraining));
                        break;
                    }
                }
            } else if (trainings instanceof Result.Error) {
                Toast.makeText(getContext(), "Error retrieving trainings: " + ((Result.Error) trainings).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<RoutinesCard> generateCardsFromTraining(Training training) {
        List<RoutinesCard> cards = new ArrayList<>();
        if (training != null && training.getRoutinesList() != null) {
            for (Routine day : training.getRoutinesList()) {
                // Manteniamo "Exercises" ma i nomi delle routine sono dinamici
                cards.add(new RoutinesCard(day.getName(), "Exercises: " + day.getWorkoutTotalExercises(), day.getRoutineId()));
            }
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
            Bundle args = new Bundle();
            args.putString("trainingId", trainingId);
            args.putString("dayId", card.getTrainingDayId());
            Navigation.findNavController(getView()).navigate(R.id.nav_training_days_to_edit, args);
        }
    }

    private void startNewWorkout(RoutinesCard card) {
        if (currentTraining == null || card.getTrainingDayId() == null) return;
        for (Routine day : currentTraining.getRoutinesList()) {
            if (card.getTrainingDayId().equals(day.getRoutineId())) {
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", day);
                args.putSerializable("parentTraining", currentTraining);
                NavHostFragment.findNavController(this).navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    private void showReplaceWorkoutDialog(RoutinesCard card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout in Progress")
                .setMessage("You already have an active workout session. Would you like to discard it and start a new one?")
                .setPositiveButton("Discard and Start", (dialog, which) -> {
                    workoutViewModel.stopWorkout(new FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            startNewWorkout(card);
                        }
                        @Override
                        public void onError(Exception e) {
                            startNewWorkout(card);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}