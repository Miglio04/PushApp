package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Serie;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for managing and displaying the list of exercises in the request routine editing screen.
 * Handles expandable items, allowing adding, editing, and deleting sets and exercises.
 */
public class EditRoutineAdapter extends RecyclerView.Adapter<EditRoutineAdapter.ViewHolder> {

    private List<WorkoutExercise> workoutExercises;
    private final OnExerciseInteractionListener listener;

    /**
     * Interface for handling interaction events on exercises and sets.
     */
    public interface OnExerciseInteractionListener {
        void onEditExercise(int position);
        void onDeleteExercise(int position);
        void onSetCreated(int exercisePosition);
        void onSetUpdated(int exercisePosition, int setPosition, double newWeight, int newReps);
        void onSetDeleted(int exercisePosition, int setPosition);

    }

    /**
     * Constructs a new EditRoutineAdapter.
     *
     * @param workoutExercises The initial list of exercises.
     * @param listener         The listener for interaction events.
     */
    public EditRoutineAdapter(List<WorkoutExercise> workoutExercises, OnExerciseInteractionListener listener) {
        this.workoutExercises = workoutExercises != null ? workoutExercises : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the list of exercises and refreshes the RecyclerView.
     *
     * @param newWorkoutExercises The new list of exercises.
     */
    public void setExercises(List<WorkoutExercise> newWorkoutExercises) {
        this.workoutExercises = newWorkoutExercises != null ? newWorkoutExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for an exercise item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_expandable, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * Handles view expansion and sets up click listeners for exercise actions.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutExercise workoutExercise = workoutExercises.get(position);

        holder.nameTextView.setText(workoutExercise.getExerciseName());

        boolean isExpanded = workoutExercise.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.arrowIcon.setRotation(isExpanded ? 180f : 0f);

        holder.headerLayout.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                boolean expanded = workoutExercise.isExpanded();
                workoutExercise.setExpanded(!expanded);
                notifyItemChanged(currentPos);
            }
        });

        holder.btnDeleteExercise.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onDeleteExercise(currentPos);
            }
        });

        holder.btnEditExercise.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onEditExercise(currentPos);
            }
        });

        holder.btnAddSet.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onSetCreated(currentPos);
            }
        });

        holder.setupInnerRecyclerView(workoutExercise.getSeries(), listener);
    }

    /**
     * Returns the total number of exercises.
     *
     * @return The size of the workout exercises list.
     */
    @Override
    public int getItemCount() {
        return workoutExercises.size();
    }

    /**
     * ViewHolder class for caching view references for an exercise item.
     * Manages the inner RecyclerView for sets.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTextView;
        public final ImageView arrowIcon;
        public final LinearLayout headerLayout;
        public final LinearLayout expandableLayout;
        public final ImageButton btnEditExercise;
        public final ImageButton btnDeleteExercise;
        public final ImageButton btnAddSet;
        final RecyclerView recyclerSeries;
        private SetsAdapter setsAdapter;

        /**
         * Constructs a new ViewHolder.
         *
         * @param view The item view.
         */
        public ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.exercise_name);
            arrowIcon = view.findViewById(R.id.arrow_icon);
            headerLayout = view.findViewById(R.id.header_layout);
            expandableLayout = view.findViewById(R.id.expandable_layout);
            btnEditExercise = view.findViewById(R.id.btn_edit_exercise);
            btnDeleteExercise = view.findViewById(R.id.btn_delete_exercise);
            btnAddSet = view.findViewById(R.id.btn_add_set);

            recyclerSeries = view.findViewById(R.id.sets_recycler_view);
        }

        /**
         * Sets up the inner RecyclerView for displaying sets associated with this exercise.
         *
         * @param series       The list of sets (series) for this exercise.
         * @param mainListener The listener to propagate events to the parent adapter.
         */
        void setupInnerRecyclerView(List<Serie> series, OnExerciseInteractionListener mainListener) {
            recyclerSeries.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            SetsAdapter.OnSetInteractionListener innerListener = new SetsAdapter.OnSetInteractionListener() {
                @Override
                public void onSetUpdated(int setPosition, double newWeight, int newReps) {
                    int exercisePosition = getBindingAdapterPosition();
                    if (mainListener != null && exercisePosition != RecyclerView.NO_POSITION) {
                        mainListener.onSetUpdated(exercisePosition, setPosition, newWeight, newReps);
                    }
                }

                @Override
                public void onSetDeleted(int setPosition) {
                    int exercisePosition = getBindingAdapterPosition();
                    if (mainListener != null && exercisePosition != RecyclerView.NO_POSITION) {
                        mainListener.onSetDeleted(exercisePosition, setPosition);
                    }
                }
            };

            List<Serie> safeSeries = series != null ? series : new ArrayList<>();
            setsAdapter = new SetsAdapter(safeSeries, innerListener);
            recyclerSeries.setAdapter(setsAdapter);
        }
    }
}