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
import com.example.pushapp.models.Training;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment implements WorkoutExerciseAdapter.OnWorkoutInteractionListener {

    private WorkoutViewModel workoutViewModel;
    private WorkoutExerciseAdapter workoutAdapter;
    private ImageButton workoutBackButton;
    private RecyclerView recyclerView;
    private TextView timerText;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private TextView headerTitle;

    // UI REST TIMER
    private View restTimerContainer;
    private TextView restTimerText;
    private ProgressBar restTimerProgress;
    private Button restTimerSkip;

    public WorkoutFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usiamo requireActivity() così il ViewModel sopravvive alla navigazione e permette il ripristino sessione
        workoutViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelFactory(requireContext())).get(WorkoutViewModel.class);

        // LOGICA DI AVVIO: Se entriamo con nuovi argomenti e non c'è un workout attivo, resetta e parti
        if (getArguments() != null) {
            Boolean inProgress = workoutViewModel.isWorkoutInProgress().getValue();
            if (inProgress == null || !inProgress) {
                Routine dayToStart = (Routine) getArguments().getSerializable("trainingDay");
                if (dayToStart != null) {
                    workoutViewModel.startWorkout(dayToStart);
                }
            }
        }
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

        // Stile One UI: Nasconde la barra di navigazione per dare focus all'allenamento
        updateGlobalUIVisibility(false);
    }

    private void initViews(View view) {
        workoutBackButton = view.findViewById(R.id.workout_back_button);
        headerTitle = view.findViewById(R.id.header_title);
        timerText = view.findViewById(R.id.workout_timer_text);
        startPauseButton = view.findViewById(R.id.workout_start_pause_button);
        stopButton = view.findViewById(R.id.workout_stop_button);
        recyclerView = view.findViewById(R.id.recycler_workout);

        // Container del timer di riposo (Azzurrino)
        restTimerContainer = view.findViewById(R.id.rest_timer_container);
        restTimerText = view.findViewById(R.id.rest_timer_text);
        restTimerProgress = view.findViewById(R.id.rest_timer_progress);
        restTimerSkip = view.findViewById(R.id.rest_timer_skip);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutAdapter = new WorkoutExerciseAdapter(new ArrayList<>(), new ArrayList<>(), this);
        recyclerView.setAdapter(workoutAdapter);
    }

    private void setupObservers() {
        // Timer principale del workout
        workoutViewModel.getFormattedTime().observe(getViewLifecycleOwner(), time -> timerText.setText(time));

        // Titolo (es: "Leg Day")
        workoutViewModel.getWorkoutTitle().observe(getViewLifecycleOwner(), title -> headerTitle.setText(title));

        // Icona Play/Pause
        workoutViewModel.isWorkoutTimerRunning().observe(getViewLifecycleOwner(), this::updateStartPauseIcon);

        // Lista esercizi: quando cambia (o viene resettata), l'adapter si aggiorna
        workoutViewModel.getActiveWorkoutSession().observe(getViewLifecycleOwner(), currentSession -> {
            if (currentSession != null && currentSession.exercises != null) {
                Routine originalRoutine = workoutViewModel.getOriginalRoutineTemplate();
                List<WorkoutExercise> templateExercises = (originalRoutine != null) ? originalRoutine.getWorkoutExercises() : new ArrayList<>();
                workoutAdapter.setExercises(currentSession.exercises, templateExercises);
            } else {
                workoutAdapter.setExercises(new ArrayList<>(), new ArrayList<>());
            }
        });

        // VISIBILITÀ REST TIMER
        workoutViewModel.isRestTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            restTimerContainer.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        });

        // AGGIORNAMENTO REST TIMER (Sotto-secondi e progress)
        workoutViewModel.getRestSecondsRemaining().observe(getViewLifecycleOwner(), seconds -> {
            restTimerText.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));

            // Colore Rosso se mancano meno di 5 secondi, altrimenti Azzurro Primario
            if (seconds <= 5 && seconds > 0) {
                restTimerText.setTextColor(Color.RED);
            } else {
                restTimerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
            }

            Integer total = workoutViewModel.getRestTotalSeconds().getValue();
            if (total != null && total > 0) {
                restTimerProgress.setProgress((int) ((seconds * 100f) / total));
            }
        });
    }

    private void setupClickListeners() {
        // Tasto indietro (Miniplayer apparirà automaticamente se il workout è in corso)
        workoutBackButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        startPauseButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(workoutViewModel.isWorkoutTimerRunning().getValue())) {
                workoutViewModel.pauseWorkoutTimer();
            } else {
                workoutViewModel.startWorkoutTimer();
            }
        });

        // SALVATAGGIO FINALE
        stopButton.setOnClickListener(v -> {
            stopButton.setEnabled(false); // Prevenzione click multipli
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

    // --- CALLBACK DALL'ADAPTER (INTERAZIONI REALI) ---

    @Override
    public void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds) {
        // Notifica il ViewModel: questo attiverà il timer e salverà lo stato nel SessionManager
        workoutViewModel.toggleSetCompleted(exercisePosition, setPosition, restTimeSeconds);
    }

    @Override
    public void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps) {
        // Salvataggio istantaneo mentre l'utente digita
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

    // --- METODI HELPER ---

    private void updateStartPauseIcon(boolean isRunning) {
        startPauseButton.setImageResource(isRunning ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private void updateGlobalUIVisibility(boolean show) {
        View nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setVisibility(show ? View.VISIBLE : View.GONE);

        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null) mini.setVisibility(show && Boolean.TRUE.equals(workoutViewModel.isWorkoutInProgress().getValue()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Quando il fragment viene distrutto (es: torniamo indietro), mostriamo il mini-player e la nav
        updateGlobalUIVisibility(true);
    }
}