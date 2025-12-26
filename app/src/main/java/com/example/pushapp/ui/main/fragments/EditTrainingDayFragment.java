package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.utils.EditTrainingDayAdapter;
import com.example.pushapp.utils.TrainingListGenerator;
import com.example.pushapp.utils.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EditTrainingDayFragment extends Fragment implements EditTrainingDayAdapter.OnExerciseInteractionListener {
    private String trainingDayId;
    private String trainingId;
    private TrainingViewModel viewModel;
    private EditTrainingDayAdapter adapter;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabAddExercise;

    public EditTrainingDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        if (getArguments() != null) {
            trainingDayId = getArguments().getString("trainingDayId");
            trainingId = getArguments().getString("trainingId");
            viewModel.loadTrainingDayForEdit(trainingId, trainingDayId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_training_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Inizializzazione Viste ---
        toolbar = view.findViewById(R.id.toolbar_edit_day);
        fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_exercises);

        // --- Setup dei componenti ---
        setupRecyclerView(recyclerView);
        setupToolbar();
        setupFab();
        observeViewModel();

    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Crea l'adapter UNA SOLA VOLTA, con una lista vuota e passando 'this' come listener
        adapter = new EditTrainingDayAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        // Imposta l'azione per il bottone "Indietro"
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Infla il menu che contiene il bottone "Salva"
        toolbar.inflateMenu(R.menu.edit_mode_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveChanges();
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAddExercise.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void observeViewModel() {
        // Osserva i dati del giorno di allenamento. Quando cambiano, aggiorna la UI.
        viewModel.getEditableTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                toolbar.setTitle(trainingDay.getName()); // Aggiorna il titolo della toolbar
                if (trainingDay.getExercises() != null) {
                    adapter.setExercises(trainingDay.getExercises()); // Aggiorna la lista nell'adapter
                }
            }
        });
    }

    private void saveChanges() {
        // TODO: In futuro, questo chiamerà viewModel.saveTrainingDayChanges(trainingId);
        // per salvare le modifiche su Firebase tramite il repository.
        Toast.makeText(getContext(), "Modifiche salvate (simulazione)", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).popBackStack(); // Torna alla schermata precedente
    }

    private void showAddExerciseDialog() {
        // Dialog per inserire il nome del nuovo esercizio
        final EditText input = new EditText(requireContext());
        input.setHint("Nome Esercizio");

        new AlertDialog.Builder(requireContext())
                .setTitle("Aggiungi Nuovo Esercizio")
                .setView(input)
                .setPositiveButton("Aggiungi", (dialog, which) -> {
                    String exerciseName = input.getText().toString().trim();
                    if (!exerciseName.isEmpty()) {
                        TrainingDay currentDay = viewModel.getEditableTrainingDay().getValue();
                        int order = (currentDay != null && currentDay.getExercises() != null) ? currentDay.getExercises().size() + 1 : 1;

                        // Crea un nuovo esercizio e lo passa al ViewModel, che si occuperà di aggiornare lo stato
                        Exercise newExercise = new Exercise(exerciseName, "N/A", order);
                        viewModel.addExerciseToDay(newExercise);
                    } else {
                        Toast.makeText(getContext(), "Il nome non può essere vuoto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public void onEditExercise(int position) {
        // 1. Ottieni la lista di esercizi disponibili dal nostro generatore
        List<Exercise> availableExercises = TrainingListGenerator.getAvailableExercises();

        // 2. Prepara un array di stringhe (i nomi degli esercizi) da mostrare nel dialog
        //    CharSequence è compatibile con il metodo setItems di AlertDialog.
        final CharSequence[] exerciseNames = new CharSequence[availableExercises.size()];
        for (int i = 0; i < availableExercises.size(); i++) {
            exerciseNames[i] = availableExercises.get(i).getName();
        }

        // 3. Costruisci e mostra l'AlertDialog con la lista
        new AlertDialog.Builder(requireContext())
                .setTitle("Scegli un nuovo esercizio")
                .setItems(exerciseNames, (dialog, which) -> {
                    // 'which' è l'indice dell'elemento cliccato nella lista 'exerciseNames'

                    // 4. Ottieni il nuovo esercizio scelto dall'utente
                    Exercise selectedExercise = availableExercises.get(which);

                    // 5. Comunica al ViewModel di sostituire l'esercizio
                    viewModel.replaceExerciseInDay(position, selectedExercise);

                    Toast.makeText(getContext(), "Esercizio cambiato in " + selectedExercise.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null) // Aggiungiamo un bottone per annullare
                .show();
    }

    @Override
    public void onDeleteExercise(int position) {
        // Mostra un dialog di conferma prima di eliminare
        final Exercise exerciseToDelete = viewModel.getEditableTrainingDay().getValue().getExercises().get(position);

        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setMessage("Sei sicuro di voler eliminare l'esercizio \"" + exerciseToDelete.getName() + "\"?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    // L'azione viene inoltrata al ViewModel
                    viewModel.deleteExerciseFromDay(position);
                    Toast.makeText(getContext(), "\"" + exerciseToDelete.getName() + "\" eliminato!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public void onSetUpdated(int exercisePosition, int setPosition, double newWeight, int newReps) {
        // Inoltra l'evento al ViewModel
        viewModel.updateSetInExercise(exercisePosition, setPosition, newWeight, newReps);
    }

    @Override
    public void onSetDeleted(int exercisePosition, int setPosition) {
        // Mostra un dialog di conferma prima di inoltrare al ViewModel
        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma Eliminazione Serie")
                .setMessage("Sei sicuro di voler eliminare questa serie?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    viewModel.deleteSetFromExercise(exercisePosition, setPosition);
                    Toast.makeText(getContext(), "Serie eliminata", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

}
