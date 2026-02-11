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
import com.example.pushapp.models.history.HistorySerie;

import java.util.List;

public class WorkoutSessionSetAdapter extends RecyclerView.Adapter<WorkoutSessionSetAdapter.ViewHolder> {

    private final List<HistorySerie> series;
    private final List<Serie> templateSeries;
    private final OnSessionSetListener listener;

    public interface OnSessionSetListener {
        void onSetCompleted(int position);
        void onSetDataChanged(int position, double actualWeight, int actualReps);
        void onSetDeleted(int position);
    }

    public WorkoutSessionSetAdapter(List<HistorySerie> series, List<Serie> templateSeries, OnSessionSetListener listener) {
        this.series = series;
        this.templateSeries = templateSeries;
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
        HistorySerie serie = series.get(position);
        Serie templateSerie = null;
        if (templateSeries != null && position < templateSeries.size()) {
            templateSerie = templateSeries.get(position);
        }

        holder.setNumber.setText(String.valueOf(serie.getSetNumber()));
        if (templateSerie != null) {
            String target = templateSerie.getTargetWeight() + " kg x " + templateSerie.getTargetReps() + " reps";
            holder.targetDetails.setText(target);
            holder.targetDetails.setVisibility(View.VISIBLE);
        } else {
            holder.targetDetails.setVisibility(View.GONE);
        }

        if (holder.weightWatcher != null) holder.actualWeight.removeTextChangedListener(holder.weightWatcher);
        if (holder.repsWatcher != null) holder.actualReps.removeTextChangedListener(holder.repsWatcher);

        holder.actualWeight.setText(serie.getWeight() > 0 ? String.valueOf(serie.getWeight()) : "");
        holder.actualReps.setText(serie.getReps() > 0 ? String.valueOf(serie.getReps()) : "");

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

        holder.completeButton.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onSetCompleted(currentPos);
            }
        });
        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onSetDeleted(pos);
            }
        });

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
        } else {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_off_background);
        }
        holder.actualWeight.setEnabled(!completed);
        holder.actualReps.setEnabled(!completed);
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