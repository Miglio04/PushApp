package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.WorkoutViewModel;
// 1. Importa il NUOVO adapter per gli esercizi durante l'allenamento
import com.example.pushapp.utils.WorkoutExerciseAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

// 2. Implementa la NUOVA interfaccia, che ora ha un solo metodo
public class WorkoutFragment extends Fragment implements WorkoutExerciseAdapter.OnWorkoutInteractionListener {

    // I tuoi campi rimangono invariati
    private WorkoutViewModel workoutViewModel;
    private WorkoutExerciseAdapter workoutAdapter; // <-- Usa il nuovo adapter
    private ImageButton workoutBackButton;
    private RecyclerView recyclerView;
    private TextView timerText;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private TextView headerTitle;
    private View restTimerContainer;
    private TextView restTimerText;
    private ProgressBar restTimerProgress;
    private Button restTimerSkip;

    public WorkoutFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        if (getArguments() != null) {
            // Assicurati che le classi modello implementino Serializable
            if ((workoutViewModel.isWorkoutInProgress().getValue() == null || !workoutViewModel.isWorkoutInProgress().getValue())) {
                TrainingDay dayToStart = (TrainingDay) getArguments().getSerializable("trainingDay");
                Training parentTraining = (Training) getArguments().getSerializable("parentTraining");

                if (dayToStart != null) {
                    workoutViewModel.startWorkout(dayToStart, parentTraining);
                }
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
        // L'inizializzazione delle viste è corretta e rimane invariata
        workoutBackButton = view.findViewById(R.id.workout_back_button);
        headerTitle = view.findViewById(R.id.header_title);
        timerText = view.findViewById(R.id.workout_timer_text);
        startPauseButton = view.findViewById(R.id.workout_start_pause_button);
        stopButton = view.findViewById(R.id.workout_stop_button);
        recyclerView = view.findViewById(R.id.recycler_workout);
        restTimerContainer = view.findViewById(R.id.rest_timer_container);
        restTimerText = restTimerContainer.findViewById(R.id.rest_timer_text);
        restTimerProgress = restTimerContainer.findViewById(R.id.rest_timer_progress);
        restTimerSkip = restTimerContainer.findViewById(R.id.rest_timer_skip);

        // Gli observer per i timer e i bottoni sono corretti e rimangono invariati
        workoutViewModel.getFormattedTime().observe(getViewLifecycleOwner(), time -> timerText.setText(time));
        workoutViewModel.getWorkoutTitle().observe(getViewLifecycleOwner(), title -> headerTitle.setText(title));
        workoutViewModel.isWorkoutTimerRunning().observe(getViewLifecycleOwner(), this::updateStartPauseIcon);
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

        workoutBackButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        startPauseButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(workoutViewModel.isWorkoutTimerRunning().getValue())) {
                workoutViewModel.pauseWorkoutTimer();
            } else {
                workoutViewModel.startWorkoutTimer();
            }
        });
        stopButton.setOnClickListener(v -> {
            workoutViewModel.stopWorkout(new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    NavHostFragment.findNavController(WorkoutFragment.this).popBackStack();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Errore nel salvataggio", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(WorkoutFragment.this).popBackStack();
                }
            });
        });
        restTimerSkip.setOnClickListener(v -> workoutViewModel.skipRestTimer());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // --- INIZIO BLOCCO DA MODIFICARE ---

        // 3. Crea il NUOVO adapter
        workoutAdapter = new WorkoutExerciseAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(workoutAdapter);

        // 4. Osserva il NUOVO LiveData
        workoutViewModel.getActiveTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                // Passa la lista di Exercise al nuovo adapter
                workoutAdapter.setExercises(trainingDay.getExercises());
            }
        });

        // --- FINE BLOCCO DA MODIFICARE ---

        // La logica per nascondere la bottom nav e il mini-player è corretta
        hideBottomNav();
        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null) {
            mini.setVisibility(View.GONE);
        }
    }

    // --- Implementazione della NUOVA interfaccia ---
    // 5. Implementa il SOLO metodo richiesto dalla nuova interfaccia
    @Override
    public void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        workoutViewModel.toggleSetCompleted(exercisePosition, setPosition, restTimeSeconds);
    }

    @Override
    public void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps) {
        workoutViewModel.updateSetData(exercisePosition, setPosition, actualWeight, actualReps);
    }

    @Override
    public void onAddSet(int exercisePosition) {
        workoutViewModel.addSetToExercise(exercisePosition);
    }

    @Override
    public void onSetDeleted(int exercisePosition, int setPosition) {
        workoutViewModel.deleteSetFromExercise(exercisePosition, setPosition);
    }


    // I metodi helper rimangono invariati
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
