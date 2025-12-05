package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.pushapp.R;
import com.example.pushapp.utils.TrainingListGenerator;
import com.example.pushapp.utils.TrainingsAdapter;

public class TrainingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trainings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TrainingsAdapter adapter = new TrainingsAdapter(getContext(),
                R.layout.training_card_view,
                TrainingListGenerator.generateTrainingList());

        Log.println(Log.DEBUG, "Debug", TrainingListGenerator.generateTrainingList().toString());

        ListView listView = view.findViewById(R.id.training_list);
        listView.setAdapter(adapter);
    }
}