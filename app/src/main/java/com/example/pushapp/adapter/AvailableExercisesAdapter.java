package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.models.api.ExerciseApiModel;
import com.google.android.material.chip.Chip;
import java.util.List;

public class AvailableExercisesAdapter extends RecyclerView.Adapter<AvailableExercisesAdapter.ExerciseViewHolder> {

    private List<ExerciseApiModel> exercises;
    private final OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseApiModel exercise);
    }

    public AvailableExercisesAdapter(List<ExerciseApiModel> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_selectable, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseApiModel exercise = exercises.get(position);
        holder.bind(exercise, listener);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void updateExercises(List<ExerciseApiModel> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Chip muscle;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_exercise_name);
            muscle = itemView.findViewById(R.id.chip_exercise_muscle);
        }

        public void bind(final ExerciseApiModel exercise, final OnExerciseClickListener listener) {
            name.setText(exercise.getName());
            muscle.setText(capitalize(exercise.getMuscle()));
            itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).replace("_", " ");
        }
    }
}
