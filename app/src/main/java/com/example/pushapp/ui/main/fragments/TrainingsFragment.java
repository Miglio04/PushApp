package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;
import com.example.pushapp.utils.TrainingListGenerator;
import com.example.pushapp.utils.TrainingsAdapter;
import com.example.pushapp.utils.TrainingsRecyclerViewAdapter;

import java.util.ArrayList;

public class TrainingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trainings, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ArrayList<Training> trainings = TrainingListGenerator.generateTrainingList();

        TrainingsRecyclerViewAdapter adapter = new TrainingsRecyclerViewAdapter(trainings);

        RecyclerView recyclerView = view.findViewById(R.id.training_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);
    }
}