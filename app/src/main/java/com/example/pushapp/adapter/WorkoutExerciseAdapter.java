package com.example.pushapp.adapter;

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
import com.example.pushapp.models.WorkoutExercise;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {

    private List<WorkoutExercise> workoutExercises;
    private final OnWorkoutInteractionListener listener;

    public interface OnWorkoutInteractionListener {
        void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds);
        void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps);
        void onAddSet(int exercisePosition);
        void onSetDeleted(int exercisePosition, int setPosition);
    }

    public WorkoutExerciseAdapter(List<WorkoutExercise> workoutExercises, OnWorkoutInteractionListener listener) {
        this.workoutExercises = workoutExercises != null ? workoutExercises : new ArrayList<>();
        this.listener = listener;
    }

    public void setExercises(List<WorkoutExercise> newWorkoutExercises) {
        this.workoutExercises = newWorkoutExercises != null ? newWorkoutExercises : new ArrayList<>();
        // Il notifyDataSetChanged è fondamentale qui per "pulire" visivamente tutto il layout
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
        WorkoutExercise workoutExercise = workoutExercises.get(position);

        holder.cardTitle.setText(workoutExercise.getApiExerciseId());
        holder.cardDescription.setText(workoutExercise.getSeries() != null ?
                workoutExercise.getSeries().size() + " serie" : "0 serie");

        // Gestione corretta del plurale per le serie
        int setCount = workoutExercise.getSeries() != null ? workoutExercise.getSeries().size() : 0;
        holder.cardDescription.setText(setCount + (" serie"));

        // --- CONFIGURAZIONE SPINNER RIPOSO ---
        String[] restTimes = {"30s", "60s", "90s", "120s", "180s"};
        int[] restValues = {30, 60, 90, 120, 180};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                R.layout.item_spinner_custom, // Usa il tuo layout azzurrino se esiste, o android.R.layout.simple_spinner_item
                restTimes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.restSpinner.setAdapter(spinnerAdapter);

        // Imposta la selezione salvata o default (60s)
        holder.restSpinner.setSelection(workoutExercise.getRestTimeIndex());

        holder.restSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                workoutExercise.setRestTimeIndex(pos);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // --- INNER ADAPTER (SERIE) ---
        // Definiamo il listener per le singole serie
        WorkoutSessionSetAdapter.OnSessionSetListener innerListener = new WorkoutSessionSetAdapter.OnSessionSetListener() {
            @Override
            public void onSetCompleted(int setPosition) {
                // Recuperiamo il tempo di riposo attuale dallo spinner
                int selectedIndex = holder.restSpinner.getSelectedItemPosition();
                int restSeconds = (selectedIndex >= 0) ? restValues[selectedIndex] : 60;

                // NOTA: Usiamo holder.getBindingAdapterPosition() per evitare errori di indice durante lo scroll
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onSetCompleted(currentPos, setPosition, restSeconds);
                }
            }

            @Override
            public void onSetDataChanged(int setPosition, double actualWeight, int actualReps) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onSetDataChanged(currentPos, setPosition, actualWeight, actualReps);
                }
            }

            @Override
            public void onSetDeleted(int setPosition) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onSetDeleted(currentPos, setPosition);
                }
            }
        };

        holder.addSetButton.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onAddSet(currentPos);
            }
        });

        // BUG FIX: Creiamo sempre un nuovo adapter o aggiorniamo i dati per evitare "Ghost Data" nelle EditText
        WorkoutSessionSetAdapter setAdapter = new WorkoutSessionSetAdapter(workoutExercise.getSeries(), innerListener);
        holder.setsRecyclerView.setAdapter(setAdapter);
    }

    @Override
    public int getItemCount() {
        return workoutExercises != null ? workoutExercises.size() : 0;
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

            // Importante: settiamo il layout manager qui una volta sola
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            // Disabilitiamo il nested scrolling per rendere lo scroll della card fluido
            setsRecyclerView.setNestedScrollingEnabled(false);
        }
    }
}