// Sostituisci l'intero contenuto di TrainingsFragment.java con questo
package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.example.pushapp.models.Training;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.TrainingListGenerator;
import com.example.pushapp.utils.TrainingViewModel;
import com.example.pushapp.utils.TrainingsRecyclerViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class TrainingsFragment extends Fragment implements TrainingsRecyclerViewAdapter.OnTrainingInteractionListener {

    private TrainingViewModel viewModel;
    private TrainingsRecyclerViewAdapter adapter;
    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("TrainingsFragment", "onViewCreated called");

        navController = Navigation.findNavController(view);

        // 1. Inizializza il ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TrainingViewModel.class);

        // 2. Setup della RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.training_list);
        setupRecyclerView(recyclerView);

        // 3. Setup dei bottoni
        FloatingActionButton fab = view.findViewById(R.id.fab_add_training);
        fab.setOnClickListener(v -> showCreateTrainingDialog());

        Button btnAddSampleData = view.findViewById(R.id.btn_add_sample_data);
        btnAddSampleData.setOnClickListener(v -> {
            addSampleDataToFirebase();
            Toast.makeText(getContext(), "Adding sample data to Firebase...", Toast.LENGTH_SHORT).show();
        });

        // 4. Osserva i dati dal ViewModel
        observeViewModel();

        // 5. Carica i dati iniziali da Firebase
        viewModel.loadTrainings();
        Log.d("TrainingsFragment", "loadTrainings() called");
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrainingsRecyclerViewAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings != null) {
                Log.d("TrainingsFragment", "Received " + trainings.size() + " trainings:");
                for (Training t : trainings) {
                    Log.d("TrainingsFragment", "  - ID: " + t.getId() + ", Name: " + t.getName());
                }
                adapter.updateTrainings(trainings); // Assicurati che questo metodo esista nell'adapter
            }
        });

        // Observer per i messaggi di errore
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTrainingClicked(Training training) {
        Bundle bundle = new Bundle();
        bundle.putString("trainingId", training.getId());
        navController.navigate(R.id.nav_training_to_training_days, bundle);
    }

    @Override
    public void onTrainingDeleteClicked(Training training) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setMessage("Sei sicuro di voler eliminare la scheda '" + training.getName() + "'?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    viewModel.deleteTraining(training.getId(), new FirebaseCallback<Void>() {
                        @Override public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Scheda eliminata", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(Exception e) {
                            Toast.makeText(getContext(), "Errore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public void onTrainingEditFinished(Training training, String newName, String newDescription) {
        training.setName(newName);
        training.setDescription(newDescription);
        viewModel.updateTraining(training, new FirebaseCallback<Void>() {
            @Override public void onSuccess(Void result) {
                Toast.makeText(getContext(), "Scheda aggiornata", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(getContext(), "Errore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleDataToFirebase() {
        ArrayList<Training> sampleTrainings = TrainingListGenerator.generateTrainingList();
        for (Training training : sampleTrainings) {
            viewModel.createTraining(training, new FirebaseCallback<String>() {
                @Override
                public void onSuccess(String newTrainingId) {
                    Log.d("TrainingsFragment", "Sample training created with ID: " + newTrainingId);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("TrainingsFragment", "Failed to create sample training", e);
                    Toast.makeText(getContext(), "Error creating sample data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Metodo per mostrare il dialog di creazione, puoi personalizzarlo
    private void showCreateTrainingDialog() {
        // Qui puoi inserire la logica per un dialog che chiede nome e descrizione
        // e poi chiama viewModel.createTraining(...)
        Toast.makeText(getContext(), "TODO: Implement create dialog", Toast.LENGTH_SHORT).show();
    }

}
