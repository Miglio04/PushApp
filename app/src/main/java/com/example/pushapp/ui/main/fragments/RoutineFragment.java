package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.Routine;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.adapter.RoutineCardAdapter;
import com.example.pushapp.utils.DeleteDialogHelper;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying the list of routines within a specific training plan.
 * Allows users to view, add, edit, delete routines, and start a workout session from a routine.
 */
public class RoutineFragment extends Fragment {

    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private RoutineCardAdapter adapter;
    private Training currentTraining;
    private TextView headerTitle;
    private View emptyStateContainer;
    private RecyclerView recyclerView;

    public RoutineFragment() {

    }

    /**
     * Initializes ViewModels and retrieves navigation arguments (trainingId).
     *
     * @param savedInstanceState Saved state bundle.
     */
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

    /**
     * Inflates the layout for the routines list.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routines, container, false);
    }

    /**
     * Sets up the RecyclerView, observers, and floating action button listener after view creation.
     *
     * @param view               The root view.
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headerTitle = view.findViewById(R.id.header_title);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        recyclerView = view.findViewById(R.id.recycler_training_days);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            NavHostFragment.findNavController(this).popBackStack()
        );

        setupRecyclerView(recyclerView);
        observeViewModel();

        FloatingActionButton addRoutineButton = view.findViewById(R.id.fab_add_routine);
        if(addRoutineButton != null) {
            addRoutineButton.setOnClickListener(v -> trainingViewModel.createRoutine(currentTraining));
        }
    }

    /**
     * Configures the RecyclerView and sets up the adapter with callback listeners for card actions.
     *
     * @param recyclerView The RecyclerView to configure.
     */
    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoutineCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.setStartWorkoutListener(this::handleStartWorkoutClick);
        adapter.setEditRoutineListener(this::handleEditRoutineClick);
        adapter.setDeleteRoutineListener(this::handleDeleteRoutine);
    }

    /**
     * Observes changes in the training list to update the current training context and displayed routines.
     */
    private void observeViewModel() {
        trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings instanceof Result.TrainingsSuccess) {
                List<Training> trainingsList = ((Result.TrainingsSuccess) trainings).getData();
                for (Training training : trainingsList) {
                    if (trainingId.equals(training.getTrainingId())) {
                        currentTraining = training;

                        if (headerTitle != null && training.getName() != null) {
                            headerTitle.setText(training.getName());
                        }

                        List<Routine> routines = training.getRoutinesList();
                        adapter.updateCards(routines);
                        updateEmptyState(routines == null || routines.isEmpty());
                        break;
                    }
                }
            } else if (trainings instanceof Result.Error) {
                Toast.makeText(getContext(), "Error retrieving trainings: " + ((Result.Error) trainings).getMessage(), Toast.LENGTH_LONG).show();
                updateEmptyState(true);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateContainer != null && recyclerView != null) {
            emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Handles the click action to start a workout from a routine.
     * Checks if a workout is already in progress and prompts usage accordingly.
     *
     * @param routine The routine selected to start.
     */
    private void handleStartWorkoutClick(Routine routine) {
        Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();
        if (Boolean.TRUE.equals(isWorkoutInProgress)) {
            showReplaceWorkoutDialog(routine);
        } else {
            startNewWorkout(routine);
        }
    }

    /**
     * Navigates to the edit screen for the selected routine.
     *
     * @param routine The routine to be edited.
     */
    private void handleEditRoutineClick(Routine routine) {
        if (getView() != null && routine.getRoutineId() != null) {
            Bundle args = new Bundle();
            args.putString("trainingId", trainingId);
            args.putString("dayId", routine.getRoutineId());
            Navigation.findNavController(getView()).navigate(R.id.nav_training_days_to_edit, args);
        }
    }

    /**
     * Displays a confirmation dialog to delete the selected routine.
     *
     * @param routine The routine to be deleted.
     */
    private void handleDeleteRoutine(Routine routine){
        if (getView() != null && routine.getRoutineId() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_routine_title))
                    .setMessage(getString(R.string.delete_routine_message))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        trainingViewModel.deleteRoutine(routine);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    /**
     * Initiates a new workout session with the selected routine.
     *
     * @param routine The routine to start.
     */
    private void startNewWorkout(Routine routine) {
        if (currentTraining == null || routine.getRoutineId() == null) return;
        for (Routine day : currentTraining.getRoutinesList()) {
            if (routine.getRoutineId().equals(day.getRoutineId())) {
                Bundle args = new Bundle();
                args.putSerializable("trainingDay", day);
                args.putSerializable("parentTraining", currentTraining);
                NavHostFragment.findNavController(this).navigate(R.id.nav_workouts, args);
                break;
            }
        }
    }

    /**
     * Shows a dialog asking the user to discard the current active workout before starting a new one.
     *
     * @param routine The new routine the user wants to start.
     */
    private void showReplaceWorkoutDialog(Routine routine) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.workout_in_progress))
                .setMessage(getString(R.string.already_active_session))
                .setPositiveButton(getString(R.string.discard_and_start), (dialog, which) -> {
                    workoutViewModel.stopAndDiscardWorkout(new FirebaseCallback<Void>() {
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
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}