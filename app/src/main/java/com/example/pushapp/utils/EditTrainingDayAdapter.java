package com.example.pushapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;

import java.util.List;

public class EditTrainingDayAdapter extends RecyclerView.Adapter<EditTrainingDayAdapter.ViewHolder> {

    private List<Exercise> exercises;

    public interface OnExerciseInteractionListener {
        void onEditExercise(int position);
        void onDeleteExercise(int position);
        void onSetUpdated(int exercisePosition, int setPosition, double newWeight, int newReps);
        void onSetDeleted(int exercisePosition, int setPosition);
    }

    private final OnExerciseInteractionListener listener;

    public EditTrainingDayAdapter(List<Exercise> exercises, OnExerciseInteractionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    public void setExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_expandable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.nameTextView.setText(exercise.getName());

        boolean isExpanded = exercise.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.arrowIcon.setRotation(isExpanded ? 180f : 0f);

        holder.headerLayout.setOnClickListener(v -> {
            exercise.setExpanded(!exercise.isExpanded());
            notifyItemChanged(holder.getBindingAdapterPosition());
        });

        holder.btnDeleteExercise.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteExercise(holder.getBindingAdapterPosition());
            }
        });

        holder.btnEditExercise.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditExercise(holder.getBindingAdapterPosition());
            }
        });

        holder.setupInnerRecyclerView(exercise.getSeries(), listener, holder.getBindingAdapterPosition());

    }

    @Override
    public int getItemCount() {
        return exercises == null ? 0 : exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTextView;
        public final ImageView arrowIcon;
        public final LinearLayout headerLayout;
        public final LinearLayout expandableLayout;
        public final ImageButton btnEditExercise;
        public final ImageButton btnDeleteExercise;
        final RecyclerView recyclerSeries;
        private SetsAdapter setsAdapter;

        public ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.exercise_name);
            arrowIcon = view.findViewById(R.id.arrow_icon);
            headerLayout = view.findViewById(R.id.header_layout);
            expandableLayout = view.findViewById(R.id.expandable_layout);
            btnEditExercise = view.findViewById(R.id.btn_edit_exercise);
            btnDeleteExercise = view.findViewById(R.id.btn_delete_exercise);
            recyclerSeries = view.findViewById(R.id.sets_recycler_view);
        }

            // Metodo helper per configurare il RecyclerView interno
            void setupInnerRecyclerView(java.util.List<Serie> series, OnExerciseInteractionListener mainListener, int exercisePosition) {
                recyclerSeries.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

                // 2. Crea il listener per l'adapter interno
                SetsAdapter.OnSetInteractionListener innerListener = new SetsAdapter.OnSetInteractionListener() {
                    @Override
                    public void onSetUpdated(int setPosition, double newWeight, int newReps) {
                        // 3. Propaga l'evento verso l'esterno, aggiungendo l'indice dell'esercizio
                        if (mainListener != null) {
                            mainListener.onSetUpdated(exercisePosition, setPosition, newWeight, newReps);
                        }
                    }

                    @Override
                    public void onSetDeleted(int setPosition) {
                        // 3. Propaga l'evento verso l'esterno
                        if (mainListener != null) {
                            mainListener.onSetDeleted(exercisePosition, setPosition);
                        }
                    }
                };

                // 4. Crea e imposta l'adapter interno
                setsAdapter = new SetsAdapter(series, innerListener);
                recyclerSeries.setAdapter(setsAdapter);
        }
    }
}
