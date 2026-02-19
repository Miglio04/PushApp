package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of workout exercises during an active workout session.
 * Manages the display of exercise cards, including exercise details, rest timers, and the list of sets (series).
 */
public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {

    private List<WorkoutExercise> templateExercises = new ArrayList<>();
    private List<HistoryWorkoutExerciseWithSeries> workoutExercisesWithSeries = new ArrayList<>();
    private final OnWorkoutInteractionListener listener;
    private static final int[] REST_VALUES = {30, 60, 90, 120, 180};
    private static final String[] REST_TIMES = {"30s", "60s", "90s", "120s", "180s"};

    /**
     * Interface for handling interactions within a specific workout exercise card.
     */
    public interface OnWorkoutInteractionListener {
        void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds);
        void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps);
        void onAddSet(int exercisePosition);
        void onSetDeleted(int exercisePosition, int setPosition);
        void onRestTimeChanged(int exercisePosition, int newRestTimeIndex);
    }

    /**
     * Constructs a new WorkoutExerciseAdapter.
     *
     * @param workoutExercises   The list of history exercises with their series.
     * @param templateExercises  The list of template exercises from the routine.
     * @param listener           The listener for user interactions.
     */
    public WorkoutExerciseAdapter(
            List<HistoryWorkoutExerciseWithSeries> workoutExercises,
            List<WorkoutExercise> templateExercises,
            OnWorkoutInteractionListener listener) {
        this.workoutExercisesWithSeries = workoutExercises != null ? workoutExercises : new ArrayList<>();
        this.templateExercises = templateExercises != null ? templateExercises : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the list of exercises and refreshes the adapter.
     *
     * @param newWorkoutExercises   The new list of history exercises.
     * @param newTemplateExercises  The new list of template exercises.
     */
    public void setExercises(
            List<HistoryWorkoutExerciseWithSeries> newWorkoutExercises,
            List<WorkoutExercise> newTemplateExercises) {
        this.workoutExercisesWithSeries = newWorkoutExercises != null ? newWorkoutExercises : new ArrayList<>();
        this.templateExercises = newTemplateExercises != null ? newTemplateExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Creates a new ExerciseViewHolder.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type.
     * @return A new ExerciseViewHolder instance.
     */
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_card, parent, false);
        return new ExerciseViewHolder(view, listener, REST_VALUES, REST_TIMES);
    }

    /**
     * Binds data to the ExerciseViewHolder at the specified position.
     * Matches the history exercise with its corresponding template to display targets.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        HistoryWorkoutExerciseWithSeries exerciseWithSeries = workoutExercisesWithSeries.get(position);

        WorkoutExercise templateExercise = null;
        if (templateExercises != null && position < templateExercises.size()) {
            templateExercise = templateExercises.get(position);
        }

        holder.bind(exerciseWithSeries, templateExercise);
    }

    /**
     * Returns the total number of exercises.
     *
     * @return The size of the completed exercises list.
     */
    @Override
    public int getItemCount() {
        return workoutExercisesWithSeries != null ? workoutExercisesWithSeries.size() : 0;
    }

    /**
     * ViewHolder class for managing the view of a single exercise card.
     * Handles the inner RecyclerView for sets and spinner for rest time.
     */
    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        final TextView cardTitle;
        final Spinner restSpinner;
        final View restTimeChip;
        final TextView restTimeValue;
        final RecyclerView setsRecyclerView;
        final Button addSetButton;

        private final OnWorkoutInteractionListener listener;
        private final WorkoutSessionSetAdapter setAdapter;
        private final int[] restValues;
        private final String[] restTimes;

        /**
         * Constructs a new ExerciseViewHolder.
         *
         * @param itemView   The item view.
         * @param listener   The interaction listener.
         * @param restValues Array of rest time values in seconds.
         * @param restTimes  Array of display strings for rest times.
         */
        public ExerciseViewHolder(@NonNull View itemView, OnWorkoutInteractionListener listener, int[] restValues, String[] restTimes) {
            super(itemView);
            this.listener = listener;
            this.restValues = restValues;
            this.restTimes = restTimes;

            cardTitle = itemView.findViewById(R.id.card_title);
            restSpinner = itemView.findViewById(R.id.card_rest_spinner);
            restTimeChip = itemView.findViewById(R.id.rest_time_chip);
            restTimeValue = itemView.findViewById(R.id.rest_time_value);
            setsRecyclerView = itemView.findViewById(R.id.card_sets_recycler);
            addSetButton = itemView.findViewById(R.id.card_add_set);

            setupRecyclerView();
            setupRestSpinner(restTimes);
            setupListeners();

            setAdapter = new WorkoutSessionSetAdapter(new ArrayList<>(), new ArrayList<>(), createInnerSetListener());
            setsRecyclerView.setAdapter(setAdapter);
        }

        /**
         * Binds exercise data to the view components.
         * Initializes the inner sets adapter and updates the rest time spinner.
         *
         * @param exerciseWithSeries The history exercise object containing sets.
         * @param templateExercise   The template exercise object containing target sets.
         */
        public void bind(HistoryWorkoutExerciseWithSeries exerciseWithSeries, WorkoutExercise templateExercise) {
            HistoryWorkoutExercise historyExercise = exerciseWithSeries.historyWorkoutExercise;
            List<HistorySerie> historySeries = exerciseWithSeries.historySeries;
            List<Serie> templateSeries = (templateExercise != null) ? templateExercise.getSeries() : new ArrayList<>();

            cardTitle.setText(historyExercise.getExerciseName());
            setAdapter.updateData(historySeries, templateSeries);

            int initialIndex = historyExercise.getCurrentRestTimeIndex();
            if (initialIndex >= 0 && initialIndex < restValues.length) {
                if (restSpinner.getSelectedItemPosition() != initialIndex) {
                    restSpinner.setSelection(initialIndex, false);
                }
                restTimeValue.setText(restTimes[initialIndex]);
            } else {
                restSpinner.setSelection(2, false);
                restTimeValue.setText(restTimes[2]);
            }
        }

        private void setupRecyclerView() {
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            setsRecyclerView.setNestedScrollingEnabled(false);
        }

        private void setupRestSpinner(String[] restTimes) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.item_spinner_selected,
                    restTimes);
            spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
            restSpinner.setAdapter(spinnerAdapter);
        }

        private void setupListeners() {
            addSetButton.setOnClickListener(v -> {
                int currentPos = getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onAddSet(currentPos);
                }
            });

            // Click sul chip apre lo spinner
            restTimeChip.setOnClickListener(v -> restSpinner.performClick());

            restSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    int currentPos = getBindingAdapterPosition();
                    if (currentPos != RecyclerView.NO_POSITION) {
                        listener.onRestTimeChanged(currentPos, pos);
                    }
                    // Aggiorna la TextView del valore
                    if (pos >= 0 && pos < restTimes.length) {
                        restTimeValue.setText(restTimes[pos]);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        /**
         * Creates a listener for the inner sets adapter that delegates events to the main listener.
         *
         * @return An instance of OnSessionSetListener.
         */
        private WorkoutSessionSetAdapter.OnSessionSetListener createInnerSetListener() {
            return new WorkoutSessionSetAdapter.OnSessionSetListener() {
                @Override
                public void onSetCompleted(int setPosition) {
                    int currentPos = getBindingAdapterPosition();
                    if (currentPos != RecyclerView.NO_POSITION) {
                        int selectedIndex = restSpinner.getSelectedItemPosition();
                        int restSeconds = (selectedIndex >= 0 && selectedIndex < restValues.length) ? restValues[selectedIndex] : 60;
                        listener.onSetCompleted(currentPos, setPosition, restSeconds);
                    }
                }

                @Override
                public void onSetDataChanged(int setPosition, double actualWeight, int actualReps) {
                    int currentPos = getBindingAdapterPosition();
                    if (currentPos != RecyclerView.NO_POSITION) {
                        listener.onSetDataChanged(currentPos, setPosition, actualWeight, actualReps);
                    }
                }

                @Override
                public void onSetDeleted(int setPosition) {
                    int currentPos = getBindingAdapterPosition();
                    if (currentPos != RecyclerView.NO_POSITION) {
                        listener.onSetDeleted(currentPos, setPosition);
                    }
                }
            };
        }
    }
}