package com.example.pushapp.ui.main.fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
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
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.api.ExerciseApiModel;
import com.example.pushapp.models.Routine;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.adapter.AvailableExercisesAdapter;
import com.example.pushapp.adapter.EditRoutineAdapter;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class EditRoutineFragment extends Fragment implements EditRoutineAdapter.OnExerciseInteractionListener {

    private String trainingDayId;
    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private EditRoutineAdapter adapter;
    private AvailableExercisesAdapter availableExercisesAdapter;
    private MaterialToolbar toolbar;
    private ConstraintLayout searchPanel;
    private RecyclerView mainRecyclerView;
    private FloatingActionButton fabAddExercise;
    private SearchView searchView;
    private ChipGroup chipGroupMuscles;
    private ChipGroup chipGroupDifficulty;
    private String currentQuery = "";
    private String currentMuscleFilter = "Tutti";
    private String currentDifficultyFilter = "Tutti";
    private boolean areFilterComponentsInitialized = false;

    public EditRoutineFragment() {
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

        toolbar = view.findViewById(R.id.toolbar_edit_day);
        mainRecyclerView = view.findViewById(R.id.recycler_exercises);
        fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        searchPanel = view.findViewById(R.id.search_panel_container);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_edit);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_edit);

        // Correzione colori Chip Statici ("Tutti")
        applyDynamicColorsToStaticChips(view);

        trainingViewModel.loadAvailableExercises();
        setupToolbar();
        setupMainRecyclerView();
        setupBackButtonHandler();
        observeViewModel();

        fabAddExercise.setOnClickListener(v -> initializeAndShowSearchPanel());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupMainRecyclerView() {
        if (mainRecyclerView != null) {
            mainRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EditRoutineAdapter(new ArrayList<>(), this);
            mainRecyclerView.setAdapter(adapter);
        }
    }

    private void initializeAndShowSearchPanel() {
        if (getView() == null || searchPanel == null) return;
        if (!areFilterComponentsInitialized) {
            searchView = getView().findViewById(R.id.search_view);
            chipGroupMuscles = getView().findViewById(R.id.chip_group_muscle_filters);
            chipGroupDifficulty = getView().findViewById(R.id.chip_group_difficulty_filters);

            setupAvailableExercisesRecycler(getView());
            setupFilterListeners();

            trainingViewModel.getFilteredAvailableExercises().observe(getViewLifecycleOwner(), exercises -> {
                if (exercises != null && availableExercisesAdapter != null) availableExercisesAdapter.updateExercises(exercises);
            });
            trainingViewModel.getAvailableMuscleGroups().observe(getViewLifecycleOwner(), muscleGroups -> {
                if (muscleGroups != null) populateFilterChips(chipGroupMuscles, muscleGroups);
            });
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

            // --- COPIA DATI E ISTRUZIONI ---
            WorkoutExercise newWorkoutExercise = new WorkoutExercise(exerciseApiModel.getName().hashCode(), exerciseApiModel.getName(), order);

            if (exerciseApiModel.getInstructions() != null && !exerciseApiModel.getInstructions().isEmpty()) {
                newWorkoutExercise.setInstructions(exerciseApiModel.getInstructions());
            } else {
                newWorkoutExercise.setInstructions("");
            }
            // ------------------------------

            trainingViewModel.addExerciseToDay(newWorkoutExercise);
            toggleSearchPanel(false);
            Snackbar.make(requireView(), "Aggiunto: " + exerciseApiModel.getName(), Snackbar.LENGTH_SHORT).show();
        });
        filterRecycler.setAdapter(availableExercisesAdapter);
    }

    // --- IMPLEMENTAZIONE METODO MANCANTE: MOSTRA ISTRUZIONI ---
    @Override
    public void onShowInstructions(int position) {
        Routine day = trainingViewModel.getEditableTrainingDay().getValue();
        if (day == null || day.getWorkoutExercises() == null) return;

        WorkoutExercise workoutExercise = day.getWorkoutExercises().get(position);
        String instructions = workoutExercise.getInstructions();

        String message;
        if (instructions != null && !instructions.trim().isEmpty()) {
            message = instructions;
        } else {
            message = "Non sono presenti istruzioni per l'esercizio in questione.";
        }

        // Mostra Dialog
        new AlertDialog.Builder(requireContext())
                .setTitle(workoutExercise.getName())
                .setMessage(message)
                .setPositiveButton("Chiudi", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
    // ---------------------------------------------------------

    private void setupFilterListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
                return true;
            }
        });
        chipGroupMuscles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentMuscleFilter = "Tutti";
                View allChip = group.findViewById(R.id.chip_all_muscles);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentMuscleFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
        });
        chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentDifficultyFilter = "Tutti";
                View allChip = group.findViewById(R.id.chip_all_difficulty);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentDifficultyFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
        });
    }

    // --- LOGICA COLORI CHIP ---
    private void populateFilterChips(ChipGroup group, List<String> categories) {
        int childCount = group.getChildCount();
        if (childCount > 1) group.removeViews(1, childCount - 1);

        ColorStateList textColors = createDynamicColorStateList();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (String category : categories) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filter, group, false);
            chip.setText(category);
            chip.setId(View.generateViewId());
            chip.setTextColor(textColors);
            group.addView(chip);
        }
    }

    private void applyDynamicColorsToStaticChips(View view) {
        Chip chipAllMuscles = view.findViewById(R.id.chip_all_muscles);
        Chip chipAllDifficulty = view.findViewById(R.id.chip_all_difficulty);
        ColorStateList textColors = createDynamicColorStateList();
        if (chipAllMuscles != null) chipAllMuscles.setTextColor(textColors);
        if (chipAllDifficulty != null) chipAllDifficulty.setTextColor(textColors);
    }

    private ColorStateList createDynamicColorStateList() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int colorOnSurface = typedValue.data;

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { android.R.attr.state_selected },
                new int[] {}
        };
        int[] colors = new int[] {
                Color.BLACK,
                Color.BLACK,
                colorOnSurface
        };
        return new ColorStateList(states, colors);
    }

    private void toggleSearchPanel(boolean showSearch) {
        searchPanel.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        if (mainRecyclerView != null) mainRecyclerView.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (fabAddExercise != null) fabAddExercise.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View buttonsContainer = getView().findViewById(R.id.buttons_container);
        if (buttonsContainer != null) buttonsContainer.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (showSearch && searchView != null) { searchView.setIconified(false); searchView.requestFocus(); }
    }

    private void observeViewModel() {
        trainingViewModel.getEditableTrainingDay().observe(getViewLifecycleOwner(), trainingDay -> {
            if (trainingDay != null) {
                toolbar.setTitle(trainingDay.getName());
                adapter.setExercises(trainingDay.getWorkoutExercises() != null ? trainingDay.getWorkoutExercises() : new ArrayList<>());
            }
        });
        trainingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
        if (trainingId != null && trainingDayId != null) trainingViewModel.loadTrainingDayForEdit(trainingId, trainingDayId);
    }

    private void setupToolbar() { toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack()); }

    private void setupBackButtonHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (searchPanel != null && searchPanel.getVisibility() == View.VISIBLE) toggleSearchPanel(false);
                else { setEnabled(false); if (isAdded()) NavHostFragment.findNavController(EditRoutineFragment.this).popBackStack(); }
            }
        });
    }

    private void saveChanges() {
        trainingViewModel.saveTrainingDayChanges(trainingId, new FirebaseCallback<Void>() {
            @Override public void onSuccess(Void result) { Toast.makeText(getContext(), "Modifiche salvate!", Toast.LENGTH_SHORT).show(); NavHostFragment.findNavController(EditRoutineFragment.this).popBackStack(); }
            @Override public void onError(Exception e) { Toast.makeText(getContext(), "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
        });
    }

    @Override public void onEditExercise(int position) { showAddOrReplaceExerciseDialog(position); }

    @Override public void onDeleteExercise(int position) {
        Routine day = trainingViewModel.getEditableTrainingDay().getValue();
        if(day == null) return;
        new AlertDialog.Builder(requireContext()).setTitle("Elimina").setMessage("Eliminare " + day.getWorkoutExercises().get(position).getName() + "?")
                .setPositiveButton("Elimina", (dialog, which) -> trainingViewModel.deleteExerciseFromDay(position)).setNegativeButton("Annulla", null).show();
    }

    @Override public void onSetUpdated(int exPos, int setPos, double w, int r) { trainingViewModel.updateSetInExercise(exPos, setPos, w, r); }

    @Override public void onSetDeleted(int exPos, int setPos) {
        new AlertDialog.Builder(requireContext()).setTitle("Elimina Serie").setMessage("Eliminare questa serie?")
                .setPositiveButton("Elimina", (dialog, which) -> trainingViewModel.deleteSetFromExercise(exPos, setPos)).setNegativeButton("Annulla", null).show();
    }

    private void showAddOrReplaceExerciseDialog(final int positionToReplace) {
        List<ExerciseApiModel> available = trainingViewModel.getAvailableExercises().getValue();
        if (available != null && !available.isEmpty()) openSelectionDialog(available, positionToReplace);
        else {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Caricamento...");
            progressDialog.show();
            trainingViewModel.loadAvailableExercises();
            trainingViewModel.getAvailableExercises().observe(getViewLifecycleOwner(), new Observer<List<ExerciseApiModel>>() {
                @Override public void onChanged(List<ExerciseApiModel> exerciseApiModels) {
                    if (exerciseApiModels != null && !exerciseApiModels.isEmpty()) {
                        progressDialog.dismiss();
                        trainingViewModel.getAvailableExercises().removeObserver(this);
                        openSelectionDialog(exerciseApiModels, positionToReplace);
                    }
                }
            });
            new Handler().postDelayed(() -> { if (progressDialog.isShowing()) progressDialog.dismiss(); }, 5000);
        }
    }

    private void openSelectionDialog(List<ExerciseApiModel> exercises, int positionToReplace) {
        String[] names = new String[exercises.size()];
        for (int i = 0; i < exercises.size(); i++) names[i] = exercises.get(i).getName();
        new AlertDialog.Builder(requireContext()).setTitle("Sostituisci").setItems(names, (dialog, which) ->
                trainingViewModel.replaceExerciseInDay(positionToReplace, exercises.get(which))).setNegativeButton("Annulla", null).show();
    }
}