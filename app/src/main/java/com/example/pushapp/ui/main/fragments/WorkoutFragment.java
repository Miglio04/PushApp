package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
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
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Training;
// import com.example.pushapp.repositories.FirebaseCallback; // Non serve più qui
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.example.pushapp.adapter.WorkoutExerciseAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class WorkoutFragment extends Fragment implements WorkoutExerciseAdapter.OnWorkoutInteractionListener {

    private WorkoutViewModel workoutViewModel;
    private WorkoutExerciseAdapter workoutAdapter;
    private ImageButton workoutBackButton;
    private RecyclerView recyclerView;
    private TextView timerText;
    private ImageButton startPauseButton;
    private ImageButton stopButton; // Nota: Questo ora funge da pulsante "Finish"
    private TextView headerTitle;
    private View restTimerContainer;
    private TextView restTimerText;
    private ProgressBar restTimerProgress;
    private Button restTimerSkip;

    public WorkoutFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inizializza il ViewModel (Factory aggiornata)
        workoutViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelFactory(requireContext())).get(WorkoutViewModel.class);

        // 2. Avvio Workout (o Ripristino)
        if (getArguments() != null) {
            // Se il ViewModel NON ha già un workout in corso (es. ripristinato dal crash), ne avviamo uno nuovo.
            if ((workoutViewModel.isWorkoutInProgress().getValue() == null || !workoutViewModel.isWorkoutInProgress().getValue())) {
                Routine dayToStart = (Routine) getArguments().getSerializable("trainingDay");
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
        // --- INIZIALIZZAZIONE VISTE (INVARIATA) ---
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

        // --- OBSERVERS (INVARIATI) ---
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

        // --- GESTIONE BOTTONI (AGGIORNATA PER STORICO) ---

        // Tasto Indietro (Header): Chiude solo la vista, ma il workout continua sotto (nel miniplayer)
        workoutBackButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Tasto Play/Pause
        startPauseButton.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(workoutViewModel.isWorkoutTimerRunning().getValue())) {
                workoutViewModel.pauseWorkoutTimer();
            } else {
                workoutViewModel.startWorkoutTimer();
            }
        });

        // Tasto STOP/FINISH (AGGIORNATO)
        stopButton.setOnClickListener(v -> {
            // PRIMA: stopWorkout (pausa)
            // ORA: finishWorkout (Salva e Chiudi)
            workoutViewModel.finishWorkout();

            // Non navighiamo via subito! Aspettiamo che il salvataggio finisca.
            // Vedi l'observer qui sotto 'navigateToHome'.
        });

        // NUOVO: Osserva quando il salvataggio è finito per uscire
        workoutViewModel.getNavigateToHome().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (Boolean.TRUE.equals(shouldNavigate)) {
                NavController navController = NavHostFragment.findNavController(this);
                // Torna indietro (alla home o lista schede)
                navController.popBackStack();

                Toast.makeText(requireContext(), "Allenamento salvato! 💪", Toast.LENGTH_SHORT).show();

                // Reset del flag di navigazione per evitare loop se si rientra
                // (opzionale, ma buona pratica se usi SingleLiveEvent, qui usiamo MutableLiveData quindi ok)
            }
        });

        restTimerSkip.setOnClickListener(v -> workoutViewModel.skipRestTimer());

        // --- SETUP RECYCLERVIEW (INVARIATO) ---
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutAdapter = new WorkoutExerciseAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(workoutAdapter);

        workoutViewModel.getActiveTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                workoutAdapter.setExercises(trainingDay.getWorkoutExercises());
            }
        });

        // --- UI MANAGEMENT (INVARIATO) ---
        hideBottomNav();
        View mini = requireActivity().findViewById(R.id.workout_miniplayer);
        if (mini != null) {
            mini.setVisibility(View.GONE);
        }
    }

    // --- INTERFACCIA ADAPTER (INVARIATA) ---
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

    // --- METODI HELPER (INVARIATI) ---
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
        // Mostriamo il miniplayer SOLO se l'allenamento è ancora in corso (non finito/cancellato)
        if (mini != null && Boolean.TRUE.equals(workoutViewModel.isWorkoutInProgress().getValue())) {
            mini.setVisibility(View.VISIBLE);
        }
    }
}