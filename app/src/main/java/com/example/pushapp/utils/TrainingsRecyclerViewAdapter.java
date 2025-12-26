package com.example.pushapp.utils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class TrainingsRecyclerViewAdapter extends RecyclerView.Adapter<TrainingsRecyclerViewAdapter.ViewHolder> {

    private List<Training> trainings;
    private final OnTrainingInteractionListener listener;

    // 1. Interfaccia per comunicare con il Fragment
    public interface OnTrainingInteractionListener {
        void onTrainingClicked(Training training);
        void onTrainingDeleteClicked(Training training);
        void onTrainingEditFinished(Training training, String newName, String newDescription);
    }

    public TrainingsRecyclerViewAdapter(List<Training> trainings, OnTrainingInteractionListener listener) {
        this.trainings = trainings;
        this.listener = listener;
    }

    // Metodo per aggiornare i dati dal LiveData
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
        private final TextInputEditText textInputName;
        private final TextInputEditText textInputDescription;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private boolean isEditing = false;

        public ViewHolder(View view) {
            super(view);
            textInputName = view.findViewById(R.id.text_view_name);
            textInputDescription = view.findViewById(R.id.text_view_description);
            editButton = view.findViewById(R.id.edit_image_button);
            deleteButton = view.findViewById(R.id.delete_image_button);
        }

        public void bind(Training training, OnTrainingInteractionListener listener) {
            textInputName.setText(training.getName());
            textInputDescription.setText(training.getDescription());

            // Reset stato di modifica
            setEditingState(false);

            itemView.setOnClickListener(v -> listener.onTrainingClicked(training));
            deleteButton.setOnClickListener(v -> listener.onTrainingDeleteClicked(training));
            editButton.setOnClickListener(v -> {
                if (isEditing) {
                    // Clic su "Salva" (check)
                    String newName = textInputName.getText().toString();
                    String newDescription = textInputDescription.getText().toString();
                    listener.onTrainingEditFinished(training, newName, newDescription);
                }
                // Inverte lo stato di modifica
                setEditingState(!isEditing);
            });
        }

        private void setEditingState(boolean editing) {
            isEditing = editing;
            textInputName.setFocusable(editing);
            textInputDescription.setFocusable(editing);
            textInputName.setFocusableInTouchMode(editing);
            textInputDescription.setFocusableInTouchMode(editing);
            editButton.setImageResource(editing ? R.drawable.check : R.drawable.edit);
            if (editing) {
                textInputName.requestFocus();
            }
        }
    }
}
