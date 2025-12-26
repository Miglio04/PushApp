package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.api.ExerciseInfo;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.EditTrainingDayAdapter;
import com.example.pushapp.utils.TrainingViewModel;
import com.example.pushapp.utils.WorkoutViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EditTrainingDayFragment extends Fragment implements EditTrainingDayAdapter.OnExerciseInteractionListener {
    private String trainingDayId;
    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private EditTrainingDayAdapter adapter;
    private MaterialToolbar toolbar;
    // Rimuoviamo la dichiarazione duplicata del FAB da qui, verrà gestito in onViewCreated
    // private FloatingActionButton fabAddExercise;

    public EditTrainingDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            trainingDayId = getArguments().getString("trainingDayId");
            trainingId = getArguments().getString("trainingId");
            trainingViewModel.loadTrainingDayForEdit(trainingId, trainingDayId);
            workoutViewModel.loadAvailableExercises();        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_training_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar_edit_day);
        // La variabile fabAddExercise è locale a questo metodo, il che va bene
        FloatingActionButton fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_exercises);

        setupRecyclerView(recyclerView);
        setupToolbar();
        setupFab(fabAddExercise);
        observeViewModel();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EditTrainingDayAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        toolbar.inflateMenu(R.menu.edit_mode_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveChanges();
                return true;
            }
            return false;
        });
    }

    private void setupFab(FloatingActionButton fab) {
        fab.setOnClickListener(v -> showAddOrReplaceExerciseDialog(-1));
    }

    private void observeViewModel() {
        trainingViewModel.getEditableTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                toolbar.setTitle(trainingDay.getName());
                if (trainingDay.getExercises() != null) {
                    adapter.setExercises(trainingDay.getExercises());
                }
            }
        });

        trainingViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Logica per mostrare un indicatore di caricamento
        });

        trainingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveChanges() {
        trainingViewModel.saveTrainingDayChanges(trainingId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getContext(), "Modifiche salvate!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(EditTrainingDayFragment.this).popBackStack();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onEditExercise(int position) {
        showAddOrReplaceExerciseDialog(position);
    }

    private void showAddOrReplaceExerciseDialog(final int positionToReplace) {
        List<ExerciseInfo> availableExercises = workoutViewModel.getAvailableExercises().getValue();

        if (availableExercises == null || availableExercises.isEmpty()) {
            Toast.makeText(getContext(), "Caricamento lista esercizi dall'API...", Toast.LENGTH_SHORT).show();
            workoutViewModel.loadAvailableExercises();
            return;
        }

        final CharSequence[] exerciseNames = new CharSequence[availableExercises.size()];
        for (int i = 0; i < availableExercises.size(); i++) {
            exerciseNames[i] = availableExercises.get(i).getName();
        }

        String dialogTitle = (positionToReplace == -1) ? "Aggiungi Nuovo Esercizio" : "Scegli un Nuovo Esercizio";

        new AlertDialog.Builder(requireContext())
                .setTitle(dialogTitle)
                .setItems(exerciseNames, (dialog, which) -> {
                    ExerciseInfo selected = availableExercises.get(which);
                    if (positionToReplace == -1) {
                        int order = adapter.getItemCount() + 1;
                        Exercise newExercise = new Exercise(selected.getId(), selected.getName(), order);
                        trainingViewModel.addExerciseToDay(newExercise);
                    } else {
                        trainingViewModel.replaceExerciseInDay(positionToReplace, selected);
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    // Tutti gli altri metodi (onDeleteExercise, onSetUpdated, ecc.)
    // sono già corretti e non richiedono modifiche.
    @Override
    public void onDeleteExercise(int position) {
        final Exercise exerciseToDelete = trainingViewModel.getEditableTrainingDay().getValue().getExercises().get(position);

        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setMessage("Sei sicuro di voler eliminare l'esercizio \"" + exerciseToDelete.getName() + "\"?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    trainingViewModel.deleteExerciseFromDay(position);
                    Toast.makeText(getContext(), "\"" + exerciseToDelete.getName() + "\" eliminato!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public void onSetUpdated(int exercisePosition, int setPosition, double newWeight, int newReps) {
        trainingViewModel.updateSetInExercise(exercisePosition, setPosition, newWeight, newReps);
    }

    @Override
    public void onSetDeleted(int exercisePosition, int setPosition) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma Eliminazione Serie")
                .setMessage("Sei sicuro di voler eliminare questa serie?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    trainingViewModel.deleteSetFromExercise(exercisePosition, setPosition);
                    Toast.makeText(getContext(), "Serie eliminata", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }
}

