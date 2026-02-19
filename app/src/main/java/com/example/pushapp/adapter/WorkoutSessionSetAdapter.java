package com.example.pushapp.adapter;

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

/**
 * Adapter for displaying and managing sets (series) within a specific exercise during a live workout session.
 * Handles the display of target values vs actual values, completion toggling, and set deletion.
 */
public class WorkoutSessionSetAdapter extends RecyclerView.Adapter<WorkoutSessionSetAdapter.ViewHolder> {

    private final List<HistorySerie> series;
    private final List<Serie> templateSeries;
    private final OnSessionSetListener listener;

    /**
     * Listener interface for handling set-level interactions.
     */
    public interface OnSessionSetListener {
        void onSetCompleted(int position);
        void onSetDataChanged(int position, double actualWeight, int actualReps);
        void onSetDeleted(int position);
    }

    /**
     * Constructs a new WorkoutSessionSetAdapter.
     *
     * @param series         The list of history series (actual performed sets).
     * @param templateSeries The list of template series (goals/targets) from the routine.
     * @param listener       The listener for user interactions.
     */
    public WorkoutSessionSetAdapter(List<HistorySerie> series, List<Serie> templateSeries, OnSessionSetListener listener) {
        this.series = series;
        this.templateSeries = templateSeries;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder for a set item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type integer.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session_set, parent, false);
        return new ViewHolder(view, listener);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * Matches the history set with its corresponding template target if available.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorySerie serie = series.get(position);
        Serie templateSerie = null;
        if (templateSeries != null && position < templateSeries.size()) {
            templateSerie = templateSeries.get(position);
        }

        holder.bind(serie, templateSerie);
    }

    /**
     * Returns the total number of sets.
     *
     * @return The size of the series list.
     */
    @Override
    public int getItemCount() {
        return series != null ? series.size() : 0;
    }

    /**
     * ViewHolder class for caching view references for a set item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber, targetDetails;
        final EditText actualWeight, actualReps;
        final View completeButton;
        final ImageButton deleteButton;
        private final OnSessionSetListener listener;

        /**
         * Constructs a ViewHolder and initializes view references.
         *
         * @param itemView The root view of the item.
         * @param listener The listener for events.
         */
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

        /**
         * Binds data to the UI elements.
         * Sets text watchers, target display, and checkbox state.
         *
         * @param serie         The actual set data.
         * @param templateSerie The target set data (can be null).
         */
        public void bind(HistorySerie serie, Serie templateSerie) {
            setNumber.setText(String.valueOf(serie.getSetNumber()));

            if (templateSerie != null) {
                String target = String.format(Locale.getDefault(), "%.1f kg × %d",
                        templateSerie.getTargetWeight(), templateSerie.getTargetReps());
                target = target.replace(".", ",");
                targetDetails.setText(target);
                targetDetails.setVisibility(View.VISIBLE);
            } else {
                targetDetails.setVisibility(View.GONE);
            }

            if (!actualWeight.hasFocus()) {
                if (serie.getWeight() > 0) {
                    double weight = serie.getWeight();
                    if (weight == Math.floor(weight)) {
                        actualWeight.setText(String.valueOf((int) weight));
                    } else {
                        actualWeight.setText(String.format(Locale.ITALIAN, "%.1f", weight));
                    }
                } else {
                    actualWeight.setText("");
                }
            }

            if (!actualReps.hasFocus()) {
                actualReps.setText(serie.getReps() > 0 ? String.valueOf(serie.getReps()) : "");
            }

            updateCompletedUI(serie.getIsCompleted());
        }

        private void setupListeners() {
            completeButton.setOnClickListener(v -> {
                clearFocusFromInputs();
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

            actualWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateSetData();
                }
            });

            actualReps.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateSetData();
                }
            });

            actualWeight.setOnEditorActionListener((v, actionId, event) -> {
                clearFocusFromInputs();
                return true;
            });

            actualReps.setOnEditorActionListener((v, actionId, event) -> {
                clearFocusFromInputs();
                return true;
            });
        }

        /**
         * Clears focus from input fields and hides the keyboard.
         */
        private void clearFocusFromInputs() {
            actualWeight.clearFocus();
            actualReps.clearFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    itemView.getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
            }
        }

        /**
         * Parses input from EditTexts and notifies the listener of data changes.
         * Ignores incomplete input ending with comma or dot.
         */
        private void updateSetData() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            try {
                String wStr = actualWeight.getText().toString();
                String rStr = actualReps.getText().toString();

                if (wStr.endsWith(",") || wStr.endsWith(".")) {
                    return;
                }

                wStr = wStr.replace(",", ".");
                double weight = wStr.isEmpty() ? 0 : Double.parseDouble(wStr);
                int reps = rStr.isEmpty() ? 0 : Integer.parseInt(rStr);

                listener.onSetDataChanged(position, weight, reps);
            } catch (NumberFormatException ignored) {}
        }

        /**
         * Updates visual state based on whether the set is marked as completed.
         * Disables inputs if completed and fades them to show they cannot be edited.
         *
         * @param completed True if the set is complete.
         */
        private void updateCompletedUI(boolean completed) {
            if (completed) {
                completeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getColor(R.color.green)));
                actualWeight.setAlpha(0.5f);
                actualReps.setAlpha(0.5f);
                actualWeight.setTextColor(itemView.getContext().getColor(R.color.md_theme_onSurfaceVariant));
                actualReps.setTextColor(itemView.getContext().getColor(R.color.md_theme_onSurfaceVariant));
            } else {
                completeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getColor(R.color.md_theme_surfaceContainerHighest)));
                actualWeight.setAlpha(1.0f);
                actualReps.setAlpha(1.0f);
                actualWeight.setTextColor(itemView.getContext().getColor(R.color.md_theme_onSurface));
                actualReps.setTextColor(itemView.getContext().getColor(R.color.md_theme_onSurface));
            }

            actualWeight.setEnabled(!completed);
            actualReps.setEnabled(!completed);
        }
    }

    /**
     * Updates the adapter's data with new lists and refreshes the RecyclerView.
     *
     * @param newSeries         The new list of history series.
     * @param newTemplateSeries The new list of template series.
     */
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