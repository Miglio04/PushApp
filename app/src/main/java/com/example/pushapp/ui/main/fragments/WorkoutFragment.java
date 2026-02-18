package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.adapter.WorkoutExerciseAdapter;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment representing the active workout screen.
 * Displays the list of exercises, manages the workout timer, handles user interactions for completing sets,
 * and controls the rest timer UI.
 */
public class WorkoutFragment extends Fragment implements WorkoutExerciseAdapter.OnWorkoutInteractionListener {

    private WorkoutViewModel workoutViewModel;
    private WorkoutExerciseAdapter workoutAdapter;
    private ImageButton workoutBackButton;
    private RecyclerView recyclerView;
    private TextView timerText;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private TextView headerTitle;
    private View restTimerContainer;
    private TextView restTimerText;
    private int totalRestSeconds = 0;
    private ProgressBar restTimerProgress;
    private Button restTimerSkip;

    public WorkoutFragment() {}

    /**
     * Initializes the ViewModel and attempts to start or restore a workout session.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        workoutViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelFactory(requireContext())).get(WorkoutViewModel.class);

        Routine dayToStart = (getArguments() != null) ? (Routine) getArguments().getSerializable("trainingDay") : null;
        workoutViewModel.startOrRestoreWorkout(dayToStart);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
    }

    /**
     * Initializes UI components from the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view) {
        workoutBackButton = view.findViewById(R.id.workout_back_button);
        headerTitle = view.findViewById(R.id.header_title);
        timerText = view.findViewById(R.id.workout_timer_text);
        startPauseButton = view.findViewById(R.id.workout_start_pause_button);
        stopButton = view.findViewById(R.id.workout_stop_button);
        recyclerView = view.findViewById(R.id.recycler_workout);

        restTimerContainer = view.findViewById(R.id.rest_timer_container);
        restTimerText = view.findViewById(R.id.rest_timer_text);
        restTimerProgress = view.findViewById(R.id.rest_timer_progress);
        restTimerSkip = view.findViewById(R.id.rest_timer_skip);
    }

    /**
     * Configures the RecyclerView and its adapter for displaying exercises.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutAdapter = new WorkoutExerciseAdapter(new ArrayList<>(), new ArrayList<>(), this);
        recyclerView.setAdapter(workoutAdapter);
    }

    /**
     * Sets up observers for ViewModel LiveData to update the UI in response to state changes.
     */
    private void setupObservers() {
        workoutViewModel.getFormattedTime().observe(getViewLifecycleOwner(), time -> timerText.setText(time));

        workoutViewModel.getWorkoutTitle().observe(getViewLifecycleOwner(), title -> headerTitle.setText(title));

        workoutViewModel.isWorkoutTimerRunning().observe(getViewLifecycleOwner(), this::updateStartPauseIcon);

        workoutViewModel.getActiveWorkoutState().observe(getViewLifecycleOwner(), currentState -> {
            if (currentState != null && currentState.getCurrentSession() != null) {
                List<WorkoutExercise> templateExercises = (currentState.getOriginalTemplate() != null)
                        ? currentState.getOriginalTemplate().getWorkoutExercises()
                        : new ArrayList<>();
                workoutAdapter.setExercises(currentState.getCurrentSession().exercises, templateExercises);
            } else {
                workoutAdapter.setExercises(new ArrayList<>(), new ArrayList<>());
            }
        });

        workoutViewModel.isRestTimerRunning().observe(getViewLifecycleOwner(), isRunning -> restTimerContainer.setVisibility(isRunning ? View.VISIBLE : View.GONE));

        workoutViewModel.getRestTotalSeconds().observe(getViewLifecycleOwner(), total -> this.totalRestSeconds = (total != null) ? total : 0);

        workoutViewModel.getRestSecondsRemaining().observe(getViewLifecycleOwner(), seconds -> {
            restTimerText.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));

            if (seconds <= 5 && seconds > 0) {
                restTimerText.setTextColor(Color.RED);
            } else {
                restTimerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
            }

            if (totalRestSeconds > 0) {
                restTimerProgress.setProgress((int) ((seconds * 100f) / totalRestSeconds));
            } else {
                restTimerProgress.setProgress(0);
            }
        });
    }

    /**
     * Sets up click listeners for buttons (back, start/pause, stop, skip rest).
     */
    private void setupClickListeners() {
        workoutBackButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        startPauseButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(workoutViewModel.isWorkoutTimerRunning().getValue())) {
                workoutViewModel.pauseWorkoutTimer();
            } else {
                workoutViewModel.startWorkoutTimer();
            }
        });

        stopButton.setOnClickListener(v -> {
            stopButton.setEnabled(false);
            workoutViewModel.finishWorkout(() -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Workout Saved! Great job 🔥", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(WorkoutFragment.this).popBackStack();
                    });
                }
            });
        });

        restTimerSkip.setOnClickListener(v -> workoutViewModel.stopRestTimer());
    }


    /**
     * Callback when a set is marked as completed.
     * Triggers the set completion logic in the ViewModel, potentially starting a rest timer.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @param restTimeSeconds  The duration of rest associated with this set.
     */
    @Override
    public void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        workoutViewModel.toggleSetCompleted(exercisePosition, setPosition, restTimeSeconds);
    }

    /**
     * Callback when set data (weight/reps) is updated by the user.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set.
     * @param actualWeight     The new weight value.
     * @param actualReps       The new repetition count.
     */
    @Override
    public void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps) {
        workoutViewModel.updateSetData(exercisePosition, setPosition, actualWeight, actualReps);
    }

    /**
     * Callback to add a new set to an exercise.
     *
     * @param exercisePosition The index of the exercise.
     */
    @Override
    public void onAddSet(int exercisePosition) {
        workoutViewModel.addSetToExercise(exercisePosition);
    }

    /**
     * Callback to delete a specific set.
     *
     * @param exercisePosition The index of the exercise.
     * @param setPosition      The index of the set to delete.
     */
    @Override
    public void onSetDeleted(int exercisePosition, int setPosition) {
        workoutViewModel.deleteSetFromExercise(exercisePosition, setPosition);
    }

    /**
     * Callback when the rest time setting for an exercise is changed.
     *
     * @param exercisePosition The index of the exercise.
     * @param newRestTime      The new rest time value (index or seconds).
     */
    @Override
    public void onRestTimeChanged(int exercisePosition, int newRestTime) {
        workoutViewModel.updateExerciseRestTime(exercisePosition, newRestTime);
    }

    /**
     * Updates the start/pause button icon based on the timer state.
     *
     * @param isRunning True if the workout timer is running, false otherwise.
     */
    private void updateStartPauseIcon(boolean isRunning) {
        startPauseButton.setImageResource(isRunning ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    /**
     * Toggles the visibility of global UI elements like the bottom navigation and mini-player.
     * Used to hide them when the workout fragment is active and full-screen.
     *
     * @param show True to show global UI, false to hide.
     */
    private void updateGlobalUIVisibility(boolean show) {
        View nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setVisibility(show ? View.VISIBLE : View.GONE);

        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null) mini.setVisibility(show && Boolean.TRUE.equals(workoutViewModel.isWorkoutInProgress().getValue()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGlobalUIVisibility(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        updateGlobalUIVisibility(true);
    }
}