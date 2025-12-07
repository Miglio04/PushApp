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

import com.example.pushapp.R;
import com.example.pushapp.utils.TrainingCard;
import com.example.pushapp.utils.TrainingCardAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrainingDaysFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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
        List<TrainingCard> cards = generateCards(count);

        TrainingCardAdapter adapter = new TrainingCardAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

    private List<TrainingCard> generateCards(int count) {
        List<TrainingCard> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            //Stringhe provvisorie
            list.add(new TrainingCard("Routine " + i, "Routine description " + i));
        }
        return list;
    }
}