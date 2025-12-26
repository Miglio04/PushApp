package com.example.pushapp.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.models.Serie;
import java.util.List;

public class WorkoutSessionSetAdapter extends RecyclerView.Adapter<WorkoutSessionSetAdapter.ViewHolder> {

    private final List<Serie> series;
    private final OnSessionSetListener listener;

    public interface OnSessionSetListener {
        void onSetCompleted(int position);
        void onSetDataChanged(int position, double actualWeight, int actualReps);
        void onSetDeleted(int position);
    }

    public WorkoutSessionSetAdapter(List<Serie> series, OnSessionSetListener listener) {
        this.series = series;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Serie serie = series.get(position);

        holder.setNumber.setText(String.valueOf(serie.getSerieNumber()));
        String targetDetails = "Target: " + serie.getTargetWeight() + "kg x " + serie.getTargetReps();
        holder.targetDetails.setText(targetDetails);

        // Rimuovi i listener per evitare aggiornamenti indesiderati
        if (holder.weightWatcher != null) holder.actualWeight.removeTextChangedListener(holder.weightWatcher);
        if (holder.repsWatcher != null) holder.actualReps.removeTextChangedListener(holder.repsWatcher);

        // Imposta i valori attuali (se esistono)
        holder.actualWeight.setText(serie.getActualWeight() > 0 ? String.valueOf(serie.getActualWeight()) : "");
        holder.actualReps.setText(serie.getActualReps() > 0 ? String.valueOf(serie.getActualReps()) : "");

        // Aggiungi i nuovi listener
        holder.weightWatcher = createWatcher(holder, () -> {
            try {
                double weight = Double.parseDouble(holder.actualWeight.getText().toString());
                int reps = Integer.parseInt(holder.actualReps.getText().toString());
                listener.onSetDataChanged(holder.getBindingAdapterPosition(), weight, reps);
            } catch (NumberFormatException ignored) {}
        });
        holder.repsWatcher = createWatcher(holder, () -> {
            try {
                double weight = Double.parseDouble(holder.actualWeight.getText().toString());
                int reps = Integer.parseInt(holder.actualReps.getText().toString());
                listener.onSetDataChanged(holder.getBindingAdapterPosition(), weight, reps);
            } catch (NumberFormatException ignored) {}
        });
        holder.actualWeight.addTextChangedListener(holder.weightWatcher);
        holder.actualReps.addTextChangedListener(holder.repsWatcher);

        // Gestione del bottone di completamento
        holder.completeButton.setOnClickListener(v -> listener.onSetCompleted(holder.getBindingAdapterPosition()));

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onSetDeleted(pos);
            }
        });

        // Aggiorna la UI in base allo stato 'completed'
        updateCompletedUI(holder, serie.isCompleted());
    }

    @Override
    public int getItemCount() {
        return series != null ? series.size() : 0;
    }

    private void updateCompletedUI(ViewHolder holder, boolean completed) {
        // ... (la tua logica per barrare il testo e disabilitare gli EditText)
        if (completed) {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_on_background);
            // Applica stili per lo stato completato
        } else {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_off_background);
            // Rimuovi stili
        }
    }

    private TextWatcher createWatcher(ViewHolder holder, Runnable onUpdate) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { onUpdate.run(); }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber, targetDetails;
        final EditText actualWeight, actualReps;
        final ImageButton completeButton, deleteButton;
        TextWatcher weightWatcher, repsWatcher;

        public ViewHolder(View itemView) {
            super(itemView);
            setNumber = itemView.findViewById(R.id.set_number_session);
            targetDetails = itemView.findViewById(R.id.set_target_details);
            actualWeight = itemView.findViewById(R.id.set_actual_weight);
            actualReps = itemView.findViewById(R.id.set_actual_reps);
            completeButton = itemView.findViewById(R.id.set_complete_button_session);
            deleteButton = itemView.findViewById(R.id.set_delete_button_session);

        }
    }
}
