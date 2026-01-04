package com.example.pushapp.ui.main.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.ExerciseApiModel;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.AvailableExercisesAdapter;
import com.example.pushapp.utils.EditTrainingDayAdapter;
import com.example.pushapp.utils.TrainingViewModel;
import com.example.pushapp.utils.WorkoutViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class EditTrainingDayFragment extends Fragment implements EditTrainingDayAdapter.OnExerciseInteractionListener {

    private String trainingDayId;
    private String trainingId;

    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;

    private EditTrainingDayAdapter adapter;
    private AvailableExercisesAdapter availableExercisesAdapter;

    private MaterialToolbar toolbar;
    private ConstraintLayout searchPanel;
    private RecyclerView mainRecyclerView;
    private FloatingActionButton fabAddExercise;

    // Componenti Filtro
    private SearchView searchView;
    private ChipGroup chipGroupMuscles;
    private ChipGroup chipGroupEquipment;
    private ChipGroup chipGroupDifficulty; // NUOVO

    // Stato Filtri
    private String currentQuery = "";
    private String currentMuscleFilter = "Tutti";
    private String currentEquipmentFilter = "Tutti";
    private String currentDifficultyFilter = "Tutti"; // NUOVO
    private boolean areFilterComponentsInitialized = false;

    public EditTrainingDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingViewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        if (getArguments() != null) {
            trainingDayId = getArguments().getString("dayId");
            trainingId = getArguments().getString("trainingId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_training_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inizializzazione View Principali
        toolbar = view.findViewById(R.id.toolbar_edit_day);
        mainRecyclerView = view.findViewById(R.id.recycler_exercises);
        fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        searchPanel = view.findViewById(R.id.search_panel_container);

        MaterialButton btnSave = view.findViewById(R.id.btn_save_edit);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_edit);

        // Avvia caricamento esercizi
        trainingViewModel.loadAvailableExercises();

        setupToolbar();
        setupMainRecyclerView();
        setupBackButtonHandler();

        // 2. Observer
        observeViewModel();

        // 3. Listeners
        fabAddExercise.setOnClickListener(v -> initializeAndShowSearchPanel());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    // --- CONFIGURAZIONE RECYCLER VIEW PRINCIPALE ---
    private void setupMainRecyclerView() {
        if (mainRecyclerView != null) {
            mainRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EditTrainingDayAdapter(new ArrayList<>(), this);
            mainRecyclerView.setAdapter(adapter);
        }
    }

    // --- LOGICA PANNELLO DI RICERCA E FILTRI ---
    private void initializeAndShowSearchPanel() {
        if (getView() == null || searchPanel == null) return;

        if (!areFilterComponentsInitialized) {
            searchView = getView().findViewById(R.id.search_view);
            chipGroupMuscles = getView().findViewById(R.id.chip_group_muscle_filters);
            chipGroupEquipment = getView().findViewById(R.id.chip_group_equipment_filters);
            chipGroupDifficulty = getView().findViewById(R.id.chip_group_difficulty_filters); // NUOVO

            setupAvailableExercisesRecycler(getView());
            setupFilterListeners();

            // --- OBSERVERS PER I DATI DEI FILTRI ---
            trainingViewModel.getFilteredAvailableExercises().observe(getViewLifecycleOwner(), exercises -> {
                if (exercises != null && availableExercisesAdapter != null) {
                    availableExercisesAdapter.updateExercises(exercises);
                }
            });

            trainingViewModel.getAvailableMuscleGroups().observe(getViewLifecycleOwner(), muscleGroups -> {
                if (muscleGroups != null) populateFilterChips(chipGroupMuscles, muscleGroups);
            });

            trainingViewModel.getAvailableEquipment().observe(getViewLifecycleOwner(), equipmentList -> {
                if (equipmentList != null) populateFilterChips(chipGroupEquipment, equipmentList);
            });

            // NUOVO OBSERVER PER DIFFICOLTÀ
            trainingViewModel.getAvailableDifficulties().observe(getViewLifecycleOwner(), difficultyList -> {
                if (difficultyList != null) populateFilterChips(chipGroupDifficulty, difficultyList);
            });

            areFilterComponentsInitialized = true;
        }
        toggleSearchPanel(true);
    }

    private void setupAvailableExercisesRecycler(View view) {
        RecyclerView filterRecycler = view.findViewById(R.id.recycler_available_exercises);
        if (filterRecycler == null) return;

        filterRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        availableExercisesAdapter = new AvailableExercisesAdapter(new ArrayList<>(), exerciseApiModel -> {
            if (trainingViewModel.getEditableTrainingDay().getValue() == null) {
                Toast.makeText(getContext(), "Errore: Giorno non caricato.", Toast.LENGTH_SHORT).show();
                return;
            }
            int order = adapter.getItemCount() + 1;
            Exercise newExercise = new Exercise(exerciseApiModel.getName().hashCode(), exerciseApiModel.getName(), order);
            trainingViewModel.addExerciseToDay(newExercise);
            toggleSearchPanel(false);
            Snackbar.make(requireView(), "Aggiunto: " + exerciseApiModel.getName(), Snackbar.LENGTH_SHORT).show();
        });

        filterRecycler.setAdapter(availableExercisesAdapter);
    }

    // --- GESTIONE LOGICA FILTRI MULTIPLI (Query + Muscolo + Equipment + Difficoltà) ---
    private void setupFilterListeners() {
        // 1. Ricerca Testuale
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentEquipmentFilter, currentDifficultyFilter);
                return true;
            }
        });

        // 2. Filtro Muscoli
        chipGroupMuscles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentMuscleFilter = "Tutti";
                View allChip = group.findViewById(R.id.chip_all_muscles);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentMuscleFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentEquipmentFilter, currentDifficultyFilter);
        });

        // 3. Filtro Attrezzatura
        chipGroupEquipment.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentEquipmentFilter = "Tutti";
                View allChip = group.findViewById(R.id.chip_all_equipment);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentEquipmentFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentEquipmentFilter, currentDifficultyFilter);
        });

        // 4. Filtro Difficoltà (NUOVO)
        chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentDifficultyFilter = "Tutti";
                View allChip = group.findViewById(R.id.chip_all_difficulty);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentDifficultyFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentEquipmentFilter, currentDifficultyFilter);
        });
    }

    // Metodo Generico per popolare i Chip
    private void populateFilterChips(ChipGroup group, List<String> categories) {
        int childCount = group.getChildCount();
        if (childCount > 1) {
            group.removeViews(1, childCount - 1);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String category : categories) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filter, group, false);
            chip.setText(category);
            chip.setId(View.generateViewId());
            group.addView(chip);
        }
    }

    // --- GESTIONE VISIBILITÀ ---
    private void toggleSearchPanel(boolean showSearch) {
        searchPanel.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        if (mainRecyclerView != null) mainRecyclerView.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (fabAddExercise != null) fabAddExercise.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View buttonsContainer = getView().findViewById(R.id.buttons_container);
        if (buttonsContainer != null) buttonsContainer.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (showSearch && searchView != null) {
            searchView.setIconified(false);
            searchView.requestFocus();
        }
    }

    private void observeViewModel() {
        trainingViewModel.getEditableTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                toolbar.setTitle(trainingDay.getName());
                if (trainingDay.getExercises() != null) {
                    adapter.setExercises(trainingDay.getExercises());
                } else {
                    adapter.setExercises(new ArrayList<>());
                }
            }
        });
        trainingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
        if (trainingId != null && trainingDayId != null) {
            trainingViewModel.loadTrainingDayForEdit(trainingId, trainingDayId);
        }
    }

    // --- SETUP ALTRE COMPONENTI ---
    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupBackButtonHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchPanel != null && searchPanel.getVisibility() == View.VISIBLE) {
                    toggleSearchPanel(false);
                } else {
                    setEnabled(false);
                    if (isAdded()) NavHostFragment.findNavController(EditTrainingDayFragment.this).popBackStack();
                }
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
                Toast.makeText(getContext(), "Errore salvataggio: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- INTERFACCE ADAPTER ---
    @Override
    public void onEditExercise(int position) {
        showAddOrReplaceExerciseDialog(position);
    }

    @Override
    public void onDeleteExercise(int position) {
        TrainingDay day = trainingViewModel.getEditableTrainingDay().getValue();
        if(day == null) return;
        Exercise exercise = day.getExercises().get(position);
        new AlertDialog.Builder(requireContext())
                .setTitle("Elimina Esercizio")
                .setMessage("Eliminare " + exercise.getName() + "?")
                .setPositiveButton("Elimina", (dialog, which) -> trainingViewModel.deleteExerciseFromDay(position))
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
                .setTitle("Elimina Serie")
                .setMessage("Eliminare questa serie?")
                .setPositiveButton("Elimina", (dialog, which) -> trainingViewModel.deleteSetFromExercise(exercisePosition, setPosition))
                .setNegativeButton("Annulla", null)
                .show();
    }

    // --- DIALOG PER SOSTITUZIONE ---
    private void showAddOrReplaceExerciseDialog(final int positionToReplace) {
        List<ExerciseApiModel> available = trainingViewModel.getAvailableExercises().getValue();
        if (available != null && !available.isEmpty()) {
            openSelectionDialog(available, positionToReplace);
        } else {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Caricamento esercizi...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            trainingViewModel.loadAvailableExercises();
            trainingViewModel.getAvailableExercises().observe(getViewLifecycleOwner(), new Observer<List<ExerciseApiModel>>() {
                @Override
                public void onChanged(List<ExerciseApiModel> exerciseApiModels) {
                    if (exerciseApiModels != null && !exerciseApiModels.isEmpty()) {
                        progressDialog.dismiss();
                        trainingViewModel.getAvailableExercises().removeObserver(this);
                        openSelectionDialog(exerciseApiModels, positionToReplace);
                    }
                }
            });
            new Handler().postDelayed(() -> {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Errore connessione.", Toast.LENGTH_SHORT).show();
                }
            }, 5000);
        }
    }

    private void openSelectionDialog(List<ExerciseApiModel> exercises, int positionToReplace) {
        String[] names = new String[exercises.size()];
        for (int i = 0; i < exercises.size(); i++) {
            names[i] = exercises.get(i).getName();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Sostituisci Esercizio")
                .setItems(names, (dialog, which) -> {
                    trainingViewModel.replaceExerciseInDay(positionToReplace, exercises.get(which));
                })
                .setNegativeButton("Annulla", null)
                .show();
    }
}