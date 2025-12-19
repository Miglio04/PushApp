package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pushapp.R;
import com.example.pushapp.models.ExerciseSeries;
import com.example.pushapp.utils.EditTrainingDayAdapter;
import com.example.pushapp.utils.ExerciseUiModel;
import com.example.pushapp.utils.TrainingListGenerator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EditTrainingDayFragment extends Fragment {
    private int trainingDayId;
    private int trainingId;

    public EditTrainingDayFragment() {
        // Required empty public constructor
    }
    
    public static EditTrainingDayFragment newInstance() {
        return new EditTrainingDayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trainingDayId = getArguments().getInt("trainingDayId");
            trainingId = getArguments().getInt("trainingId");
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
        
        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_exercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        List<ExerciseUiModel> exerciseList = generateSampleData();
        
        EditTrainingDayAdapter adapter = new EditTrainingDayAdapter(exerciseList);
        recyclerView.setAdapter(adapter);
    }

    private List<ExerciseUiModel> generateSampleData() {
        List<ExerciseUiModel> list = new ArrayList<>();
        ArrayList<ExerciseSeries> series = TrainingListGenerator.generateExerciseSeries();
        
        for (ExerciseSeries s : series) {
            list.add(new ExerciseUiModel(s));
        }
        return list;
    }
}
