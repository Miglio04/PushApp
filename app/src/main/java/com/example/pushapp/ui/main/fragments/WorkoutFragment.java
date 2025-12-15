package com.example.pushapp.ui.main.fragments;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.pushapp.R;
import com.example.pushapp.utils.WorkoutCardAdapter;
import com.example.pushapp.utils.WorkoutSet;
import com.example.pushapp.utils.WorkoutViewModel;


public class WorkoutFragment extends Fragment implements WorkoutCardAdapter.OnCardInteractionListener {

    public static final String TAG = "WorkoutFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private WorkoutViewModel workoutViewModel;
    private ImageButton workoutBackButton;
    private RecyclerView recyclerView;
    private TextView timerText;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private TextView headerTitle;

    // Rest timer
    private View restTimerContainer;
    private TextView restTimerText;
    private ProgressBar restTimerProgress;
    private Button restTimerSkip;

    public WorkoutFragment() {}

    public static WorkoutFragment newInstance(String param1, String param2) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            // If the workout is NOT already in progress, start a new one
            if (workoutViewModel.isWorkoutInProgress().getValue() == null ||
                    Boolean.FALSE.equals(workoutViewModel.isWorkoutInProgress().getValue())) {
                workoutViewModel.startWorkout(mParam1);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        workoutBackButton = view.findViewById(R.id.workout_back_button);
        headerTitle = view.findViewById(R.id.header_title);
        timerText = view.findViewById(R.id.workout_timer_text);
        startPauseButton = view.findViewById(R.id.workout_start_pause_button);
        stopButton = view.findViewById(R.id.workout_stop_button);
        recyclerView = view.findViewById(R.id.recycler_workout);

        // Rest timer views
        restTimerContainer = view.findViewById(R.id.rest_timer_container);
        restTimerText = restTimerContainer.findViewById(R.id.rest_timer_text);
        restTimerProgress = restTimerContainer.findViewById(R.id.rest_timer_progress);
        restTimerSkip = restTimerContainer.findViewById(R.id.rest_timer_skip);

        // Observe viewmodel
        workoutViewModel.getFormattedTime().observe(getViewLifecycleOwner(), time -> timerText.setText(time));
        workoutViewModel.getWorkoutTitle().observe(getViewLifecycleOwner(), title -> headerTitle.setText(title));
        workoutViewModel.isWorkoutTimerRunning().observe(getViewLifecycleOwner(), this::updateStartPauseIcon);
        // Rest timer observers viewmodel
        workoutViewModel.isRestTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            restTimerContainer.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        });
        workoutViewModel.getRestSecondsRemaining().observe(getViewLifecycleOwner(), seconds -> {
            int mins = seconds / 60;
            int secs = seconds % 60;
            restTimerText.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));

            Integer total = workoutViewModel.getRestTotalSeconds().getValue();
            if (total != null && total > 0) {
                int progress = (int) ((seconds * 100f) / total);
                restTimerProgress.setProgress(progress);
            }
        });

        // Back button
        workoutBackButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            //workoutViewModel.pauseTimer(); // Pause the timer when leaving
        });

        // Start/Pause button
        startPauseButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(workoutViewModel.isWorkoutTimerRunning().getValue())) {
                workoutViewModel.pauseWorkoutTimer();
            } else {
                workoutViewModel.startWorkoutTimer();
            }
        });

        // Stop button
        stopButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            workoutViewModel.stopWorkout();
        });

        // Rest timer skip button
        restTimerSkip.setOnClickListener(v -> workoutViewModel.skipRestTimer());

        // Set RecyclerView's LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create the adapter with an empty list initially
        WorkoutCardAdapter adapter = new WorkoutCardAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Observe livedata for workout cards
        workoutViewModel.getWorkoutCards().observe(getViewLifecycleOwner(), cards -> {
            if (cards != null) {
                adapter.setCards(cards);
            }
        });

        // Aggiorna header con il nome della routine ricevuta
        if (mParam1 != null) headerTitle.setText(mParam1);

        // Nasconde bottom navigation in WorkoutFragment
        hideBottomNav();

        // Assicura che la mini-player sia nascosta all'ingresso in WorkoutFragment
        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null) {
            mini.setVisibility(View.GONE);
        }

    }

    // --- Implementazione dei metodi dell'interfaccia ---

    @Override
    public void onNoteChanged(int cardPosition, String newNote) {
        workoutViewModel.updateNoteForCard(cardPosition, newNote);
    }

    // Agiunge un nuovo set vuoto alla scheda specificata, si può decidere un diverso default
    @Override
    public void onAddSetClicked(int cardPosition) {
        workoutViewModel.addSetToCard(cardPosition, new WorkoutSet(0f, 0));
    }

    @Override
    public void onRestTimeChanged(int cardPosition, int newRestTime) {
        workoutViewModel.updateRestTimeForCard(cardPosition, newRestTime);
    }

    @Override
    public void onSetCompleted(int cardPosition, int setPosition) {
        workoutViewModel.setSetCompletedState(cardPosition, setPosition);
    }

    private void updateStartPauseIcon(boolean isRunning) {
        if (isRunning) {
            startPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            startPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void hideBottomNav() {
        View nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) {
            nav.setVisibility(View.GONE);
        }
    }

    private void showBottomNav() {
        View nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) {
            nav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomNav();
        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null && Boolean.TRUE.equals(workoutViewModel.isWorkoutInProgress().getValue())) {
            mini.setVisibility(View.VISIBLE);
        }
    }
}