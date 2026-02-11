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
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {

    private List<WorkoutExercise> templateExercises;
    private List<HistoryWorkoutExerciseWithSeries> workoutExercisesWithSeries;
    private final OnWorkoutInteractionListener listener;

    public interface OnWorkoutInteractionListener {
        void onSetCompleted(int exercisePosition, int setPosition, int restTimeSeconds);
        void onSetDataChanged(int exercisePosition, int setPosition, double actualWeight, int actualReps);
        void onAddSet(int exercisePosition);
        void onSetDeleted(int exercisePosition, int setPosition);
    }

    public WorkoutExerciseAdapter(
            List<HistoryWorkoutExerciseWithSeries> workoutExercises,
            List<WorkoutExercise> templateExercises,
            OnWorkoutInteractionListener listener) {
        this.workoutExercisesWithSeries = workoutExercises != null ? workoutExercises : new ArrayList<>();
        this.templateExercises = templateExercises != null ? templateExercises : new ArrayList<>();
        this.listener = listener;
    }

    public void setExercises(
            List<HistoryWorkoutExerciseWithSeries> newWorkoutExercises,
            List<WorkoutExercise> newTemplateExercises) {
        this.workoutExercisesWithSeries = newWorkoutExercises != null ? newWorkoutExercises : new ArrayList<>();
        this.templateExercises = newTemplateExercises != null ? newTemplateExercises : new ArrayList<>();
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
        HistoryWorkoutExerciseWithSeries exerciseWithSeries = workoutExercisesWithSeries.get(position);
        HistoryWorkoutExercise historyExercise = exerciseWithSeries.historyWorkoutExercise;
        List<HistorySerie> historySeries = exerciseWithSeries.historySeries;

        WorkoutExercise templateExercise = null;
        if (templateExercises != null && position < templateExercises.size()) {
            templateExercise = templateExercises.get(position);
        }
        List<Serie> templateSeries = (templateExercise != null) ? templateExercise.getSeries() : new ArrayList<>();

        holder.cardTitle.setText(historyExercise.getExerciseName());
        int setCount = historySeries != null ? historySeries.size() : 0;
        holder.cardDescription.setText(setCount + (setCount == 1 ? " serie" : " serie"));

        String[] restTimes = {"30s", "60s", "90s", "120s", "180s"};
        int[] restValues = {30, 60, 90, 120, 180};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                R.layout.item_spinner_custom,
                restTimes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.restSpinner.setAdapter(spinnerAdapter);

        int initialIndex = historyExercise.currentRestTimeIndex;
        if (initialIndex >= 0 && initialIndex < restTimes.length) {
            holder.restSpinner.setSelection(initialIndex);
        } else {
            holder.restSpinner.setSelection(2);
        }

        holder.restSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                historyExercise.currentRestTimeIndex = pos;
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        WorkoutSessionSetAdapter.OnSessionSetListener innerListener = new WorkoutSessionSetAdapter.OnSessionSetListener() {
            @Override
            public void onSetCompleted(int setPosition) {
                int selectedIndex = holder.restSpinner.getSelectedItemPosition();
                int restSeconds = (selectedIndex >= 0) ? restValues[selectedIndex] : 60;
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

        WorkoutSessionSetAdapter setAdapter = new WorkoutSessionSetAdapter(
                historySeries,
                templateSeries,
                innerListener
        );
        holder.setsRecyclerView.setAdapter(setAdapter);
    }

    @Override
    public int getItemCount() {
        return workoutExercisesWithSeries != null ? workoutExercisesWithSeries.size() : 0;
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
            setsRecyclerView.setNestedScrollingEnabled(false);
        }
    }
}