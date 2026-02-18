package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Training;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.adapter.TrainingsRecyclerViewAdapter;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.utils.DeleteDialogHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the user's list of training plans (Trainings).
 * Allows users to create, view, edit, and delete training plans.
 */
public class TrainingsFragment extends Fragment implements TrainingsRecyclerViewAdapter.OnTrainingInteractionListener {
    private UserViewModel userViewModel;
    private TrainingViewModel trainingViewModel;
    private TrainingsRecyclerViewAdapter adapter;
    private NavController navController;
    private View emptyStateContainer;
    private RecyclerView recyclerView;

    /**
     * Called when the fragment is being created.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflates the layout for the training list screen.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainings, container, false);
    }

    /**
     * Sets up views, adapters, ViewModels, and observers after the view is created.
     *
     * @param view               The root view.
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        userViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelFactory(requireContext())).get(UserViewModel.class);

        trainingViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelFactory(requireContext())).get(TrainingViewModel.class);

        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        recyclerView = view.findViewById(R.id.training_list);
        setupRecyclerView(recyclerView);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_training);
        fab.setOnClickListener(v -> {
            Result result = userViewModel.getSessionLiveData().getValue();

            if (result != null && result.isSessionSuccess()) {
                String userId = ((Result.SessionSuccess) result).getData().getUserId();
                trainingViewModel.createTraining(userId);
            }
        });
        observeViewModel();
    }

    /**
     * Configures the RecyclerView and its adapter for displaying the list of trainings.
     *
     * @param recyclerView The RecyclerView to configure.
     */
    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrainingsRecyclerViewAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Observes the training list from the ViewModel and updates the UI.
     * Handles success and error states.
     */
    private void observeViewModel() {
        trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings == null ) {
                Toast.makeText(getContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                updateEmptyState(true);
            } else if (trainings.isTrainingsSuccess()){
                List<Training> trainingsList = ((Result.TrainingsSuccess) trainings).getData();
                adapter.updateTrainings(trainingsList);
                updateEmptyState(trainingsList.isEmpty());
            }else{
                Toast.makeText(getContext(), ((Result.Error) trainings).getMessage(), Toast.LENGTH_LONG).show();
                updateEmptyState(true);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateContainer != null && recyclerView != null) {
            emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Handles clicks on a training item, navigating to the detailed schedule view.
     *
     * @param training The selected training plan.
     */
    @Override
    public void onTrainingClicked(Training training) {
        Bundle bundle = new Bundle();
        bundle.putString("trainingId", training.getTrainingId());
        navController.navigate(R.id.nav_training_to_training_days, bundle);
    }

    /**
     * Handles deletion requests for a training plan.
     * Displays a confirmation dialog before deletion.
     *
     * @param training The training plan to delete.
     */
    @Override
    public void onTrainingDeleteClicked(Training training) {
        showDeleteDialog(training);
    }

    /**
     * Called when a training plan has been edited.
     * Updates the training details in the ViewModel.
     *
     * @param training       The training plan being edited.
     */
    @Override
    public void onTrainingEditClicked(Training training) {
        showEditDialog(training);
    }

    private void showDeleteDialog(Training training) {
        if (getContext() == null) return;

        DeleteDialogHelper.show(
            requireContext(),
            R.string.delete_training_title,
            R.string.delete_training_message,
            () -> trainingViewModel.deleteTraining(training)
        );
    }

    private void showEditDialog(Training training) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_training, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        com.google.android.material.textfield.TextInputEditText etName = dialogView.findViewById(R.id.etTrainingName);
        com.google.android.material.textfield.TextInputEditText etDescription = dialogView.findViewById(R.id.etTrainingDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);


        etName.setText(training.getName());
        etDescription.setText(training.getDescription());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String newDescription = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

            if (!newName.isEmpty()) {
                training.setName(newName);
                training.setDescription(newDescription);
                trainingViewModel.updateTraining(training);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    public void onTrainingEditFinished(Training training, String newName, String newDescription) {
        training.setName(newName);
        training.setDescription(newDescription);
        trainingViewModel.updateTraining(training);
    }
}
