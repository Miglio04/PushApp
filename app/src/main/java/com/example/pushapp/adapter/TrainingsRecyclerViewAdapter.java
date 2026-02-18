package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Adapter for displaying a list of Training plans in a RecyclerView.
 * Allows viewing, editing, and deleting training plans.
 */
public class TrainingsRecyclerViewAdapter extends RecyclerView.Adapter<TrainingsRecyclerViewAdapter.ViewHolder> {
    private final List<Training> trainings;
    private final OnTrainingInteractionListener listener;

    /**
     * Interface for handling interactions with training items.
     */
    public interface OnTrainingInteractionListener {
        void onTrainingClicked(Training training);
        void onTrainingDeleteClicked(Training training);
        void onTrainingEditFinished(Training training, String newName, String newDescription);
    }

    /**
     * Constructs a new adapter.
     *
     * @param trainings The initial list of trainings.
     * @param listener  The listener for item interactions.
     */
    public TrainingsRecyclerViewAdapter(List<Training> trainings, OnTrainingInteractionListener listener) {
        this.trainings = trainings;
        this.listener = listener;
    }

    /**
     * Updates the list of trainings and refreshes the view.
     *
     * @param newTrainings The new list of trainings.
     */
    public void updateTrainings(List<Training> newTrainings) {
        this.trainings.clear();
        if (newTrainings != null) {
            this.trainings.addAll(newTrainings);
        }
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for a training item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.training_card_view, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Training training = trainings.get(position);
        holder.bind(training, listener);
    }

    /**
     * Returns the total number of training items.
     *
     * @return The size of the trainings list.
     */
    @Override
    public int getItemCount() {
        return trainings != null ? trainings.size() : 0;
    }

    /**
     * ViewHolder for caching view references of a training card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText textInputName;
        private final TextInputEditText textInputDescription;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private boolean isEditing = false;

        /**
         * Constructs a new ViewHolder.
         *
         * @param view The item view.
         */
        public ViewHolder(View view) {
            super(view);
            textInputName = view.findViewById(R.id.text_view_name);
            textInputDescription = view.findViewById(R.id.text_view_description);
            editButton = view.findViewById(R.id.edit_image_button);
            deleteButton = view.findViewById(R.id.delete_image_button);
        }

        /**
         * Binds training data to the views and sets up click listeners.
         * Handles the toggle between viewing and editing modes.
         *
         * @param training The training data to display.
         * @param listener The interaction listener.
         */
        public void bind(Training training, OnTrainingInteractionListener listener) {
            textInputName.setText(training.getName());
            textInputDescription.setText(training.getDescription());

            setEditingState(false);

            itemView.setOnClickListener(v -> listener.onTrainingClicked(training));
            deleteButton.setOnClickListener(v -> listener.onTrainingDeleteClicked(training));
            editButton.setOnClickListener(v -> {
                if (isEditing) {
                    String newName = textInputName.getText().toString();
                    String newDescription = textInputDescription.getText().toString();
                    listener.onTrainingEditFinished(training, newName, newDescription);
                }
                setEditingState(!isEditing);
            });
        }

        /**
         * Toggles the editing state of the card views.
         * Enables or disables input fields and updates the edit button icon.
         *
         * @param editing True to enable editing mode, false for viewing mode.
         */
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
