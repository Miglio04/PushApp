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
import com.google.android.material.appbar.MaterialToolbar;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.pushapp.utils.DeleteDialogHelper;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for editing an existing workout routine.
 * Allows users to modify the routine name, add/remove/replace exercises, and manage sets within exercises.
 * Also handles the UI for searching and filtering available exercises to add.
 */
public class EditRoutineFragment extends Fragment implements EditRoutineAdapter.OnExerciseInteractionListener {

    private String routineId;
    private String trainingId;
    private TrainingViewModel trainingViewModel;
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

    public EditRoutineFragment() {}

    /**
     * Initializes the ViewModels and retrieves arguments.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trainingViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(TrainingViewModel.class);

        if (getArguments() != null) {
            routineId = getArguments().getString("dayId");
            trainingId = getArguments().getString("trainingId");
        }
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_routine, container, false);
    }

    /**
     * Sets up views, listeners, and observers after the view is created.
     *
     * @param view               The root view.
     * @param savedInstanceState Saved state bundle.
     */
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

    /**
     * Configures the main RecyclerView for displaying the routine's exercises.
     */
    private void setupMainRecyclerView() {
        if (mainRecyclerView != null) {
            mainRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EditRoutineAdapter(new ArrayList<>(), this);
            mainRecyclerView.setAdapter(adapter);
        }
    }

    /**
     * Sets up the text change listener for the routine name input field.
     * Updates the ViewModel when the name changes.
     */
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

    /**
     * Sets up custom handling for the system back button.
     * Closes the search panel if open, otherwise navigates back.
     */
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

    /**
     * Initializes and displays the search panel for adding new exercises.
     * Sets up filters and adapters if not already initialized.
     */
    private void initializeAndShowSearchPanel() {
        if (getView() == null || searchPanel == null) return;

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
        } else {
            currentQuery = "";
            currentMuscleFilter = "All";
            currentDifficultyFilter = "All";
            if (searchEditText != null) {
                searchEditText.setText("");
            }
            Chip allMusclesChip = getView().findViewById(R.id.chip_all_muscles);
            if (allMusclesChip != null) allMusclesChip.setChecked(true);
            Chip allDifficultyChip = getView().findViewById(R.id.chip_all_difficulty);
            if (allDifficultyChip != null) allDifficultyChip.setChecked(true);
            trainingViewModel.applyFilters(currentQuery, currentMuscleFilter, currentDifficultyFilter);
        }
        toggleSearchPanel(true);
    }

    /**
     * Configures the RecyclerView for the list of available exercises to add.
     *
     * @param view The parent view containing the RecyclerView.
     */
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

            WorkoutExercise newWorkoutExercise = new WorkoutExercise(exercise.getName());

            trainingViewModel.addExerciseToRoutine(newWorkoutExercise);
            toggleSearchPanel(false);
        });
        filterRecycler.setAdapter(availableExercisesAdapter);
    }

    /**
     * Sets up listeners for the search view and filter chips (muscle/difficulty).
     */
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

        if (chipGroupMuscles != null) {
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
        }

        if (chipGroupDifficulty != null) {
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
    }

    /**
     * Populates a ChipGroup with dynamic filter categories.
     *
     * @param group      The ChipGroup to populate.
     * @param categories The list of category strings.
     */
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

    /**
     * Applies dynamic color logic to static "All" chips to match the theme.
     *
     * @param view The parent view.
     */
    private void applyDynamicColorsToStaticChips(View view) {
        Chip chipAllMuscles = view.findViewById(R.id.chip_all_muscles);
        Chip chipAllDifficulty = view.findViewById(R.id.chip_all_difficulty);
        ColorStateList textColors = createDynamicColorStateList();
        if (chipAllMuscles != null) chipAllMuscles.setTextColor(textColors);
        if (chipAllDifficulty != null) chipAllDifficulty.setTextColor(textColors);
    }

    /**
     * Creates a ColorStateList that changes text color based on selection state.
     *
     * @return The created ColorStateList.
     */
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

    /**
     * Shows a dialog to select an exercise effectively replacing the one at the given position.
     * If available exercises are not loaded, it prompts loading them first.
     *
     * @param positionToReplace The index of the exercise to replace.
     */
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
            trainingViewModel.getAvailableExercises().observe(getViewLifecycleOwner(), new Observer<>() {
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

    /**
     * Opens a modern dialog with a list of exercises for replacement.
     *
     * @param exercises         The list of exercises to choose from.
     * @param positionToReplace The index of the exercise to replace.
     */
    private void openSelectionDialog(List<Exercise> exercises, int positionToReplace) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_replace_exercise, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        RecyclerView rvExercises = dialogView.findViewById(R.id.rvExercises);
        EditText etSearch = dialogView.findViewById(R.id.etSearchExercise);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Exercise> filteredList = new ArrayList<>(exercises);
        AvailableExercisesAdapter dialogAdapter = new AvailableExercisesAdapter(filteredList, exercise -> {
            trainingViewModel.replaceExerciseRoutine(positionToReplace, exercise);
            dialog.dismiss();
        });
        rvExercises.setAdapter(dialogAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<Exercise> filtered = new ArrayList<>();
                for (Exercise ex : exercises) {
                    if (ex.getName().toLowerCase().contains(query)) {
                        filtered.add(ex);
                    }
                }
                dialogAdapter.updateExercises(filtered);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Toggles the visibility of the search/add exercise panel.
     *
     * @param showSearch True to show the search panel, false to hide it.
     */
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

    /**
     * Observes ViewModel data to update the UI with routine details and error messages.
     */
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

    /**
     * Saves the modifications made to the routine.
     */
    private void saveChanges() {
        Routine routine = trainingViewModel.getEditableRoutine().getValue();
        if (routine != null) {
            trainingViewModel.updateRoutine(routine);
            trainingViewModel.clearEditableRoutine();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    /**
     * Cancels the edit operation and navigates back.
     */
    private void cancelChanges() {
        trainingViewModel.clearEditableRoutine();
        NavHostFragment.findNavController(this).popBackStack();
    }

    /**
     * Called when the edit button on an exercise item is clicked.
     *
     * @param position The position of the item.
     */
    @Override public void onEditExercise(int position) {
        showAddOrReplaceExerciseDialog(position);
    }

    /**
     * Called when the delete button on an exercise item is clicked.
     * Shows a confirmation dialog.
     *
     * @param position The position of the item.
     */
    @Override public void onDeleteExercise(int position) {
        Routine routine = trainingViewModel.getEditableRoutine().getValue();
        if(routine == null || routine.getWorkoutExercises() == null) return;
        String exerciseName = routine.getWorkoutExercises().get(position).getExerciseName();
        DeleteDialogHelper.show(
            requireContext(),
            getString(R.string.delete),
            getString(R.string.delete_exercise_confirm, exerciseName),
            () -> trainingViewModel.deleteExerciseFromRoutine(position)
        );
    }

    /**
     * Called when a new set is requested for an exercise.
     *
     * @param exercisePosition The index of the exercise.
     */
    @Override
    public void onSetCreated(int exercisePosition) {
        trainingViewModel.addSetInExercise(exercisePosition);
    }

    /**
     * Called when a set is updated (weight/reps).
     *
     * @param exPos  The index of the exercise.
     * @param setPos The index of the set.
     * @param w      The new weight.
     * @param r      The new reps.
     */
    @Override public void onSetUpdated(int exPos, int setPos, double w, int r) {
        trainingViewModel.updateSetInExercise(exPos, setPos, w, r);
    }

    /**
     * Called when a set is deleted.
     * Shows a confirmation dialog.
     *
     * @param exPos  The index of the exercise.
     * @param setPos The index of the set.
     */
    @Override public void onSetDeleted(int exPos, int setPos) {
        DeleteDialogHelper.show(
            requireContext(),
            R.string.delete_set_title,
            R.string.delete_set_confirm,
            () -> trainingViewModel.deleteSetFromExercise(exPos, setPos)
        );
    }
}