package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pushapp.R;
import com.example.pushapp.utils.TrainingDaysCard;
import com.example.pushapp.utils.TrainingDaysCardAdapter;
import com.example.pushapp.utils.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

public class TrainingDaysFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private WorkoutViewModel workoutViewModel;


    public TrainingDaysFragment() { }

    public static TrainingDaysFragment newInstance(String param1, String param2) {
        TrainingDaysFragment fragment = new TrainingDaysFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training_days, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_training_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false));

        // Genera 6 schede di allenamento, esempio arbitrario
        int count = 6;
        List<TrainingDaysCard> cards = generateCards(count);

        TrainingDaysCardAdapter adapter = getTrainingDaysCardAdapter(cards);

        recyclerView.setAdapter(adapter);

    }

    @NonNull
    private TrainingDaysCardAdapter getTrainingDaysCardAdapter(List<TrainingDaysCard> cards) {
        TrainingDaysCardAdapter adapter = new TrainingDaysCardAdapter(cards);

        adapter.setOnItemClickListener(card -> {
            Boolean isWorkoutInProgress = workoutViewModel.isWorkoutInProgress().getValue();

            if (Boolean.TRUE.equals(isWorkoutInProgress)) {
                showReplaceWorkoutDialog(card);
            } else {
                startNewWorkout(card);
            }
        });
        return adapter;
    }

    private void showReplaceWorkoutDialog(TrainingDaysCard card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout in corso")
                .setMessage("Hai già un workout in corso. Vuoi scartarlo e avviarne uno nuovo?")
                .setPositiveButton("Scarta e avvia", (dialog, which) -> {
                    workoutViewModel.stopWorkout();
                    startNewWorkout(card);
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Riprendi attuale", (dialog, which) -> {
                    NavController navController = NavHostFragment.findNavController(TrainingDaysFragment.this);
                    navController.navigate(R.id.nav_workouts);
                })
                .show();
    }

    private void startNewWorkout(TrainingDaysCard card) {
        NavController navController = NavHostFragment.findNavController(TrainingDaysFragment.this);
        Bundle args = new Bundle();
        args.putString("param1", card.getTitle());
        args.putString("param2", card.getDescription());
        navController.navigate(R.id.nav_workouts, args);
    }

    private List<TrainingDaysCard> generateCards(int count) {
        List<TrainingDaysCard> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            //Stringhe provvisorie
            list.add(new TrainingDaysCard("Routine " + i, "Routine description " + i));
        }
        return list;
    }
}