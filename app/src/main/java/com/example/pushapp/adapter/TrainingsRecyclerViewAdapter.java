package com.example.pushapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;

import java.util.List;

public class TrainingsRecyclerViewAdapter extends RecyclerView.Adapter<TrainingsRecyclerViewAdapter.ViewHolder> {

    private List<Training> trainings;
    private final OnTrainingInteractionListener listener;

    public interface OnTrainingInteractionListener {
        void onTrainingClicked(Training training);
        void onTrainingDeleteClicked(Training training);
        void onTrainingEditClicked(Training training);
    }

    public TrainingsRecyclerViewAdapter(List<Training> trainings, OnTrainingInteractionListener listener) {
        this.trainings = trainings;
        this.listener = listener;
    }

    public void updateTrainings(List<Training> newTrainings) {
        this.trainings.clear();
        if (newTrainings != null) {
            this.trainings.addAll(newTrainings);
        }
        Log.d("Adapter", "Updated with " + this.trainings.size() + " trainings");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.training_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Training training = trainings.get(position);
        holder.bind(training, listener);
    }

    @Override
    public int getItemCount() {
        return trainings != null ? trainings.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewDescription;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final ImageButton arrowButton;

        public ViewHolder(View view) {
            super(view);
            textViewName = view.findViewById(R.id.text_view_name);
            textViewDescription = view.findViewById(R.id.text_view_description);
            editButton = view.findViewById(R.id.edit_image_button);
            deleteButton = view.findViewById(R.id.delete_image_button);
            arrowButton = view.findViewById(R.id.arrow_button);
        }

        public void bind(Training training, OnTrainingInteractionListener listener) {
            textViewName.setText(training.getName());
            textViewDescription.setText(training.getDescription());

            arrowButton.setOnClickListener(v -> listener.onTrainingClicked(training));
            deleteButton.setOnClickListener(v -> listener.onTrainingDeleteClicked(training));
            editButton.setOnClickListener(v -> listener.onTrainingEditClicked(training));
        }
    }
}
