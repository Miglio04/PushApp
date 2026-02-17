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
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Routine;
import com.example.pushapp.adapter.AvailableExercisesAdapter;
import com.example.pushapp.adapter.EditRoutineAdapter;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.List;

public class EditRoutineFragment extends Fragment implements EditRoutineAdapter.OnExerciseInteractionListener {

    private String routineId;
    private String trainingId;
    private TrainingViewModel trainingViewModel;
    private WorkoutViewModel workoutViewModel;
    private EditRoutineAdapter adapter;
    private AvailableExercisesAdapter availableExercisesAdapter;
    private View btnBack;
    private EditText etRoutineName;
    private ConstraintLayout searchPanel;
    private RecyclerView mainRecyclerView;
    private View fabAddExercise;
    private View btnCloseSearch;
    private EditText searchEditText;
    private ChipGroup chipGroupMuscles;
    private ChipGroup chipGroupDifficulty;
    private String currentQuery = "";
    private String currentMuscleFilter = "All";
    private String currentDifficultyFilter = "All";
    private boolean areFilterComponentsInitialized = false;

    public EditRoutineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workoutViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(WorkoutViewModel.class);

        trainingViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(TrainingViewModel.class);

        if (getArguments() != null) {
            routineId = getArguments().getString("dayId");
            trainingId = getArguments().getString("trainingId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_routine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btn_back);
        etRoutineName = view.findViewById(R.id.et_routine_name);
        mainRecyclerView = view.findViewById(R.id.recycler_exercises);
        fabAddExercise = view.findViewById(R.id.fab_add_exercise);
        searchPanel = view.findViewById(R.id.search_panel_container);
        btnCloseSearch = view.findViewById(R.id.btn_close_search);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_edit);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_edit);

        // Correzione colori Chip Statici ("All")
        applyDynamicColorsToStaticChips(view);

        setupBackButton();
        setupRoutineNameInput();
        setupMainRecyclerView();
        setupBackButtonHandler();
        observeViewModel();

        fabAddExercise.setOnClickListener(v -> initializeAndShowSearchPanel());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> cancelChanges());
    }

    private void setupMainRecyclerView() {
        if (mainRecyclerView != null) {
            mainRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EditRoutineAdapter(new ArrayList<>(), this);
            mainRecyclerView.setAdapter(adapter);
        }
    }

    private void setupRoutineNameInput() {
        etRoutineName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Routine routine = trainingViewModel.getEditableRoutine().getValue();
                if (routine != null) {
                    routine.setName(s.toString());
                }
            }
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        if (btnCloseSearch != null) {
            btnCloseSearch.setOnClickListener(v -> toggleSearchPanel(false));
        }
    }

    private void setupBackButtonHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchPanel != null && searchPanel.getVisibility() == View.VISIBLE) {
                    toggleSearchPanel(false);
                } else {
                    setEnabled(false);
                    if (isAdded()) {
                        NavHostFragment.findNavController(EditRoutineFragment.this).popBackStack();
                    }
                }
            }
        });
    }

    // --- Pagina Esercizi Disponibili ---
    private void initializeAndShowSearchPanel() {
        if (getView() == null || searchPanel == null) return;

        // Carica gli esercizi disponibili
        trainingViewModel.loadAvailableExercises();

        if (!areFilterComponentsInitialized) {
            searchEditText = getView().findViewById(R.id.search_edit_text);
            chipGroupMuscles = getView().findViewById(R.id.chip_group_muscle_filters);
            chipGroupDifficulty = getView().findViewById(R.id.chip_group_difficulty_filters);

            setupAvailableExercisesRecycler(getView());
            setupFilterListeners();

            trainingViewModel.getFilteredAvailableExercises().observe(getViewLifecycleOwner(), exercises -> {
                if (exercises != null && availableExercisesAdapter != null) {
                    if (exercises.isExerciseSuccess()) {
                        availableExercisesAdapter.updateExercises(((Result.ExerciseSuccess) exercises).getData());
                    }
                }
            });
            trainingViewModel.getAvailableMuscleGroups().observe(getViewLifecycleOwner(), muscleGroups -> {
                if (muscleGroups != null) {
                    populateFilterChips(chipGroupMuscles, muscleGroups);
                }
            });
            trainingViewModel.getAvailableDifficulties().observe(getViewLifecycleOwner(), difficultyList -> {
                if (difficultyList != null) {
                    populateFilterChips(chipGroupDifficulty, difficultyList);
                }
            });
            areFilterComponentsInitialized = true;
        }
        toggleSearchPanel(true);
    }

    private void setupAvailableExercisesRecycler(View view) {
        RecyclerView filterRecycler = view.findViewById(R.id.recycler_available_exercises);
        if (filterRecycler == null) return;
        filterRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        availableExercisesAdapter = new AvailableExercisesAdapter(new ArrayList<>(), exercise -> {
            if (trainingViewModel.getEditableRoutine().getValue() == null) {
                Toast.makeText(getContext(), R.string.error_routine_not_loaded, Toast.LENGTH_SHORT).show();
                return;
            }
            int order = adapter.getItemCount() + 1;

            WorkoutExercise newWorkoutExercise = new WorkoutExercise(exercise.getName(), order);

            trainingViewModel.addExerciseToRoutine(newWorkoutExercise);
            toggleSearchPanel(false);
        });
        filterRecycler.setAdapter(availableExercisesAdapter);
    }

    private void setupFilterListeners() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString();
                    trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        chipGroupMuscles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentMuscleFilter = "All";
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
                currentDifficultyFilter = "All";
                View allChip = group.findViewById(R.id.chip_all_difficulty);
                if (allChip instanceof Chip) ((Chip) allChip).setChecked(true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) currentDifficultyFilter = selectedChip.getText().toString();
            }
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
        });
    }

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

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };
        int[] colors = new int[]{
                Color.BLACK,
                Color.BLACK,
                colorOnSurface
        };
        return new ColorStateList(states, colors);
    }

    private void showAddOrReplaceExerciseDialog(final int positionToReplace) {
        List<Exercise> available = new ArrayList<>();
        Result result = trainingViewModel.getAvailableExercises().getValue();
        if (result != null && result.isExerciseSuccess()) {
            available = ((Result.ExerciseSuccess) result).getData();
        }
        if (available != null && !available.isEmpty()) {
            openSelectionDialog(available, positionToReplace);
        } else {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
            trainingViewModel.getAvailableExercises().observe(getViewLifecycleOwner(), new Observer<Result>() {
                @Override
                public void onChanged(Result result) {
                    if (result.isExerciseSuccess()) {
                        List<Exercise> exercises = ((Result.ExerciseSuccess) result).getData();
                        if (exercises != null && !exercises.isEmpty()) {
                            progressDialog.dismiss();
                            trainingViewModel.getAvailableExercises().removeObserver(this);
                            openSelectionDialog(exercises, positionToReplace);
                        }
                    } else if (result.isExerciseError()) {
                        progressDialog.dismiss();
                        trainingViewModel.getAvailableExercises().removeObserver(this);
                        Toast.makeText(getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            new Handler().postDelayed(() -> {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }, 5000);
        }
    }

    private void openSelectionDialog(List<Exercise> exercises, int positionToReplace) {
        String[] names = new String[exercises.size()];
        for (int i = 0; i < exercises.size(); i++) names[i] = exercises.get(i).getName();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.replace)
                .setItems(names, (dialog, which) ->
                        trainingViewModel.replaceExerciseRoutine(positionToReplace, exercises.get(which)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void toggleSearchPanel(boolean showSearch) {
        searchPanel.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        if (mainRecyclerView != null) mainRecyclerView.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (fabAddExercise != null) fabAddExercise.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View buttonsContainer = getView().findViewById(R.id.buttons_container);
        if (buttonsContainer != null) buttonsContainer.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View exercisesHeader = getView().findViewById(R.id.exercises_header);
        if (exercisesHeader != null) exercisesHeader.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View nameCard = getView().findViewById(R.id.name_card);
        if (nameCard != null) nameCard.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        View header = getView().findViewById(R.id.header);
        if (header != null) header.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        if (showSearch && searchEditText != null) {
            searchEditText.requestFocus();
        }
    }
    // -------------------------------------

    private void observeViewModel() {
        trainingViewModel.getEditableRoutine().observe(getViewLifecycleOwner(), routine -> {
            if (routine != null) {
                if (!etRoutineName.getText().toString().equals(routine.getName())) {
                    etRoutineName.setText(routine.getName());
                }
                adapter.setExercises(routine.getWorkoutExercises() != null ? routine.getWorkoutExercises() : new ArrayList<>());
            }
        });
        trainingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty())
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
        if (trainingId != null && routineId != null)
            trainingViewModel.loadRoutineForEdit(trainingId, routineId);
    }

    private void saveChanges() {
        Routine routine = trainingViewModel.getEditableRoutine().getValue();
        if (routine != null) {
            trainingViewModel.updateRoutine(routine);
            trainingViewModel.clearEditableRoutine();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void cancelChanges() {
        trainingViewModel.clearEditableRoutine();
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onEditExercise(int position) {
        showAddOrReplaceExerciseDialog(position);
    }

    @Override
    public void onDeleteExercise(int position) {
        Routine routine = trainingViewModel.getEditableRoutine().getValue();
        if (routine == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.delete_exercise_confirm, routine.getWorkoutExercises().get(position).getExerciseName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> trainingViewModel.deleteExerciseFromRoutine(position))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onSetCreated(int exercisePosition) {
        trainingViewModel.addSetInExercise(exercisePosition);
    }

    @Override
    public void onSetUpdated(int exPos, int setPos, double w, int r) {
        trainingViewModel.updateSetInExercise(exPos, setPos, w, r);
    }

    @Override
    public void onSetDeleted(int exPos, int setPos) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_set_title)
                .setMessage(R.string.delete_set_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> trainingViewModel.deleteSetFromExercise(exPos, setPos))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}