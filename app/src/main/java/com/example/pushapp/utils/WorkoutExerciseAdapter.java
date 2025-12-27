// Sostituisci l'intero contenuto di WorkoutExerciseAdapter.java con questo
package com.example.pushapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private final OnWorkoutInteractionListener listener;

    // 1. Interfaccia corretta
    public interface OnWorkoutInteractionListener {
        void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds);
        void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps);
        void onAddSet(int exercisePosition);
        void onSetDeleted(int exercisePosition, int setPosition);
    }

    public WorkoutExerciseAdapter(List<Exercise> exercises, OnWorkoutInteractionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    public void setExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_card, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        holder.cardTitle.setText(exercise.getName());
        holder.cardDescription.setText(exercise.getSeries() != null ?
                exercise.getSeries().size() + " serie" : "0 serie");

        // Configura lo spinner per il tempo di recupero
        String[] restTimes = {"30s", "60s", "90s", "120s", "180s"};
        int[] restValues = {30, 60, 90, 120, 180};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                restTimes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.restSpinner.setAdapter(spinnerAdapter);
        holder.restSpinner.setSelection(exercise.getRestTimeIndex());

        holder.restSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                exercise.setRestTimeIndex(pos);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Crea un listener per il NUOVO adapter interno (WorkoutSessionSetAdapter)
        WorkoutSessionSetAdapter.OnSessionSetListener innerListener = new WorkoutSessionSetAdapter.OnSessionSetListener() {
            @Override
            public void onSetCompleted(int setPosition) {
                int selectedIndex = holder.restSpinner.getSelectedItemPosition();
                int restSeconds = restValues[selectedIndex];
                listener.onSetCompleted(holder.getBindingAdapterPosition(), setPosition, restSeconds);
            }

            @Override
            public void onSetDataChanged(int setPosition, double actualWeight, int actualReps) {
                listener.onSetDataChanged(holder.getBindingAdapterPosition(), setPosition, actualWeight, actualReps);
            }

            @Override
            public void onSetDeleted(int setPosition) {
                listener.onSetDeleted(holder.getBindingAdapterPosition(), setPosition);
            }
        };

        // Gestisce addSetButton
        holder.addSetButton.setOnClickListener(v -> {
            listener.onAddSet(holder.getBindingAdapterPosition());
        });

        // Crea e imposta il NUOVO adapter per le serie
        WorkoutSessionSetAdapter setAdapter = new WorkoutSessionSetAdapter(exercise.getSeries(), innerListener);
        holder.setsRecyclerView.setAdapter(setAdapter);
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        final ImageView cardImage;
        final TextView cardTitle;
        final TextView cardDescription;
        final EditText cardNote;
        final Spinner restSpinner;
        final RecyclerView setsRecyclerView;
        final Button addSetButton;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
            cardTitle = itemView.findViewById(R.id.card_title);
            cardDescription = itemView.findViewById(R.id.card_description);
            cardNote = itemView.findViewById(R.id.card_note);
            restSpinner = itemView.findViewById(R.id.card_rest_spinner);
            setsRecyclerView = itemView.findViewById(R.id.card_sets_recycler);
            addSetButton = itemView.findViewById(R.id.card_add_set);
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}
