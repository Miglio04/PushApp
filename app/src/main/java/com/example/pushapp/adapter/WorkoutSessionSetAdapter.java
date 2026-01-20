package com.example.pushapp.adapter;

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
        // Assicurati che il layout sia quello giusto: item_workout_session_set
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Serie serie = series.get(position);

        // Numero Serie
        holder.setNumber.setText(String.valueOf(position + 1)); // Usa la posizione, non getSerieNumber() che potrebbe essere null

        // Dettagli Target (es. "Target: 50kg x 10")
        String targetText = String.format("Target: %.1fkg x %d", serie.getTargetWeight(), serie.getTargetReps());
        holder.targetDetails.setText(targetText);

        // Rimuovi vecchi listener
        if (holder.weightWatcher != null) holder.actualWeight.removeTextChangedListener(holder.weightWatcher);
        if (holder.repsWatcher != null) holder.actualReps.removeTextChangedListener(holder.repsWatcher);

        // Imposta valori attuali
        // Mostra il valore solo se è maggiore di 0, altrimenti lascia vuoto o mostra hint
        holder.actualWeight.setText(serie.getActualWeight() > 0 ? String.valueOf(serie.getActualWeight()) : "");
        holder.actualReps.setText(serie.getActualReps() > 0 ? String.valueOf(serie.getActualReps()) : "");

        // Logica TextWatcher per salvare i dati mentre scrivi
        holder.weightWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateSetData(holder);
            }
        };
        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateSetData(holder);
            }
        };

        holder.actualWeight.addTextChangedListener(holder.weightWatcher);
        holder.actualReps.addTextChangedListener(holder.repsWatcher);

        // Click Listener
        holder.completeButton.setOnClickListener(v -> listener.onSetCompleted(holder.getBindingAdapterPosition()));

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onSetDeleted(pos);
            }
        });

        // Aggiorna grafica (barrato/verde)
        updateCompletedUI(holder, serie.isCompleted());
    }

    private void updateSetData(ViewHolder holder) {
        try {
            String wStr = holder.actualWeight.getText().toString();
            String rStr = holder.actualReps.getText().toString();

            double weight = wStr.isEmpty() ? 0 : Double.parseDouble(wStr);
            int reps = rStr.isEmpty() ? 0 : Integer.parseInt(rStr);

            listener.onSetDataChanged(holder.getBindingAdapterPosition(), weight, reps);
        } catch (NumberFormatException ignored) {}
    }

    private void updateCompletedUI(ViewHolder holder, boolean completed) {
        if (completed) {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_on_background);
            // Opzionale: disabilitare input o barrare testo
            // holder.actualWeight.setEnabled(false);
            // holder.actualReps.setEnabled(false);
        } else {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_off_background);
            // holder.actualWeight.setEnabled(true);
            // holder.actualReps.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return series != null ? series.size() : 0;
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