package com.example.pushapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class TrainingsAdapter extends ArrayAdapter<Training>{

    private ArrayList<Training> trainings;

    private int resource;


    public TrainingsAdapter(@NonNull Context context, int resource, ArrayList<Training> trainings) {
        super(context, resource, trainings);
        this.resource = resource;
        this.trainings = trainings;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        TextView name = convertView.findViewById(R.id.text_view_name);
        TextView description = convertView.findViewById(R.id.text_view_description);
        name.setText(trainings.get(position).getName());
        description.setText(trainings.get(position).getDescription());

        return convertView;
    }
}
