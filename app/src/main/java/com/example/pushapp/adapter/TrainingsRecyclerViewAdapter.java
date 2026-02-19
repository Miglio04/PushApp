package com.example.pushapp.adapter;

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
        void onTrainingEditClicked(Training training);
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
        private final TextView textViewName;
        private final TextView textViewDescription;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final ImageButton arrowButton;

        /**
         * Constructs a new ViewHolder.
         *
         * @param view The item view.
         */
        public ViewHolder(View view) {
            super(view);
            textViewName = view.findViewById(R.id.text_view_name);
            textViewDescription = view.findViewById(R.id.text_view_description);
            editButton = view.findViewById(R.id.edit_image_button);
            deleteButton = view.findViewById(R.id.delete_image_button);
            arrowButton = view.findViewById(R.id.arrow_button);
        }

        /**
         * Binds training data to the views and sets up click listeners.
         * Handles the toggle between viewing and editing modes.
         *
         * @param training The training data to display.
         * @param listener The interaction listener.
         */
        public void bind(Training training, OnTrainingInteractionListener listener) {
            textViewName.setText(training.getName());
            textViewDescription.setText(training.getDescription());

            arrowButton.setOnClickListener(v -> listener.onTrainingClicked(training));
            deleteButton.setOnClickListener(v -> listener.onTrainingDeleteClicked(training));
            editButton.setOnClickListener(v -> listener.onTrainingEditClicked(training));
        }
    }
}
