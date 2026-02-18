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
import java.util.Locale;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session_set, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorySerie serie = series.get(position);
        Serie templateSerie = null;
        if (templateSeries != null && position < templateSeries.size()) {
            templateSerie = templateSeries.get(position);
        }

        holder.bind(serie, templateSerie);
    }

    @Override
    public int getItemCount() {
        return series != null ? series.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber, targetDetails;
        final EditText actualWeight, actualReps;
        final ImageButton completeButton, deleteButton;
        private final OnSessionSetListener listener;
        TextWatcher weightWatcher, repsWatcher;

        public ViewHolder(View itemView, OnSessionSetListener listener) {
            super(itemView);
            this.listener = listener;
            setNumber = itemView.findViewById(R.id.set_number_session);
            targetDetails = itemView.findViewById(R.id.set_target_details);
            actualWeight = itemView.findViewById(R.id.set_actual_weight);
            actualReps = itemView.findViewById(R.id.set_actual_reps);
            completeButton = itemView.findViewById(R.id.set_complete_button_session);
            deleteButton = itemView.findViewById(R.id.set_delete_button_session);
            setupListeners();
        }

        public void bind(HistorySerie serie, Serie templateSerie) {
            actualWeight.removeTextChangedListener(weightWatcher);
            actualReps.removeTextChangedListener(repsWatcher);

            setNumber.setText(String.valueOf(serie.getSetNumber()));

            if (templateSerie != null) {
                String target = String.format(Locale.getDefault(), "%.1f kg x %d reps",
                        templateSerie.getTargetWeight(), templateSerie.getTargetReps());
                targetDetails.setText(target);
                targetDetails.setVisibility(View.VISIBLE);
            } else {
                targetDetails.setVisibility(View.GONE);
            }

            actualWeight.setText(serie.getWeight() > 0 ? String.format(Locale.US, "%.1f", serie.getWeight()) : "");
            actualReps.setText(serie.getReps() > 0 ? String.valueOf(serie.getReps()) : "");

            updateCompletedUI(serie.getIsCompleted());

            actualWeight.addTextChangedListener(weightWatcher);
            actualReps.addTextChangedListener(repsWatcher);
        }

        private void setupListeners() {
            completeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSetCompleted(position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSetDeleted(position);
                }
            });

            weightWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    updateSetData();
                }
            };

            repsWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    updateSetData();
                }
            };
        }

        private void updateSetData() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            try {
                String wStr = actualWeight.getText().toString();
                String rStr = actualReps.getText().toString();

                double weight = wStr.isEmpty() ? 0 : Double.parseDouble(wStr);
                int reps = rStr.isEmpty() ? 0 : Integer.parseInt(rStr);

                listener.onSetDataChanged(position, weight, reps);
            } catch (NumberFormatException ignored) {
                // Ignora input se nel formato sbagliato, non aggiorna i dati
            }
        }

        private void updateCompletedUI(boolean completed) {
            int iconRes = completed ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background;
            completeButton.setImageResource(iconRes);

            actualWeight.setEnabled(!completed);
            actualReps.setEnabled(!completed);
        }
    }

    public void updateData(List<HistorySerie> newSeries, List<Serie> newTemplateSeries) {
        this.series.clear();
        this.series.addAll(newSeries);
        this.templateSeries.clear();
        if (newTemplateSeries != null) {
            this.templateSeries.addAll(newTemplateSeries);
        }
        notifyDataSetChanged();
    }
}