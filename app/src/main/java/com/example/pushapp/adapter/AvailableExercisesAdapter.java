package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.google.android.material.chip.Chip;
import java.util.List;

/**
 * Adapter for displaying a list of available exercises that can be selected/added to a routine.
 * Displays exercise names and muscle groups in a RecyclerView.
 */
public class AvailableExercisesAdapter extends RecyclerView.Adapter<AvailableExercisesAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private final OnExerciseClickListener listener;

    /**
     * Interface definition for a callback to be invoked when an exercise is clicked.
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    /**
     * Constructs a new AvailableExercisesAdapter.
     *
     * @param exercises The list of exercises to display.
     * @param listener  The listener for click events.
     */
    public AvailableExercisesAdapter(List<Exercise> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    /**
     * Creates a new ExerciseViewHolder.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type integer.
     * @return A new ExerciseViewHolder instance.
     */
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_selectable, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Binds formatted exercise data to the ViewHolder at the specified position.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise, listener);
    }

    /**
     * Returns the total number of exercises.
     *
     * @return The size of the exercises list.
     */
    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * Updates the list of exercises and refreshes the RecyclerView.
     *
     * @param newExercises The new list of exercises.
     */
    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for caching view references for an exercise item.
     */
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Chip muscle;

        /**
         * Constructs a new ExerciseViewHolder.
         *
         * @param itemView The item view.
         */
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_exercise_name);
            muscle = itemView.findViewById(R.id.chip_exercise_muscle);
        }

        /**
         * Binds exercise data to the views and sets the click listener.
         * Formats the muscle group string.
         *
         * @param exercise The exercise data.
         * @param listener The click listener.
         */
        public void bind(final Exercise exercise, final OnExerciseClickListener listener) {
            name.setText(exercise.getName());
            muscle.setText(capitalize(exercise.getMuscle()));
            itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));
        }

        /**
         * Capitalizes the first letter of a string and replaces underscores with spaces.
         *
         * @param str The string to format.
         * @return The formatted string.
         */
        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).replace("_", " ");
        }
    }
}
