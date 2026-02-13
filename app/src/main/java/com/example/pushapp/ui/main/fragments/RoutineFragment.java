package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.example.pushapp.adapter.RoutineCardAdapter;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutineFragment extends Fragment {

    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private RoutineCardAdapter adapter;
    private Training currentTraining;
    private FloatingActionButton addRoutineButton;

    public RoutineFragment() {

    }

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

        addRoutineButton = view.findViewById(R.id.fab_add_routine);
        if(addRoutineButton != null) {
            addRoutineButton.setOnClickListener(v -> trainingViewModel.createRoutine(currentTraining));
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoutineCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.setStartWorkoutListener(this::handleStartWorkoutClick);
        adapter.setEditRoutineListener(this::handleEditRoutineClick);
        adapter.setDeleteRoutineListener(this::handleDeleteRoutine);
    }

    private void observeViewModel() {
        trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings instanceof Result.TrainingsSuccess) {
                List<Training> trainingsList = ((Result.TrainingsSuccess) trainings).getData();
                for (Training training : trainingsList) {
                    if (trainingId.equals(training.getTrainingId())) {
                        currentTraining = training;
                        adapter.updateCards(training.getRoutinesList());
                        break;
                    }
                }
            } else if (trainings instanceof Result.Error) {
                Toast.makeText(getContext(), "Error retrieving trainings: " + ((Result.Error) trainings).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void handleStartWorkoutClick(Routine routine) {
        Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();
        if (Boolean.TRUE.equals(isWorkoutInProgress)) {
            showReplaceWorkoutDialog(routine);
        } else {
            startNewWorkout(routine);
        }
    }

    private void handleEditRoutineClick(Routine routine) {
        if (getView() != null && routine.getRoutineId() != null) {
            Bundle args = new Bundle();
            args.putString("trainingId", trainingId);
            args.putString("dayId", routine.getRoutineId());
            Navigation.findNavController(getView()).navigate(R.id.nav_training_days_to_edit, args);
        }
    }

    private void handleDeleteRoutine(Routine routine){
        if (getView() != null && routine.getRoutineId() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Routine")
                    .setMessage("Are you sure you want to delete this routine?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        trainingViewModel.deleteRoutine(routine);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void startNewWorkout(Routine routine) {
        if (currentTraining == null || routine.getRoutineId() == null) return;
        for (Routine day : currentTraining.getRoutinesList()) {
            if (routine.getRoutineId().equals(day.getRoutineId())) {
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", (Serializable) day);
                args.putSerializable("parentTraining", (Serializable) currentTraining);
                NavHostFragment.findNavController(this).navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    private void showReplaceWorkoutDialog(Routine routine) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout in Progress")
                .setMessage("You already have an active workout session. Would you like to discard it and start a new one?")
                .setPositiveButton("Discard and Start", (dialog, which) -> {
                    workoutViewModel.stopWorkout(new FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            startNewWorkout(routine);
                        }
                        @Override
                        public void onError(Exception e) {
                            startNewWorkout(routine);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}