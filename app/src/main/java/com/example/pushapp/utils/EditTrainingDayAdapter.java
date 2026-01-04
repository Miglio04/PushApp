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

import java.util.ArrayList;
import java.util.List;

public class EditTrainingDayAdapter extends RecyclerView.Adapter<EditTrainingDayAdapter.ViewHolder> {

    private List<Exercise> exercises;
    private final OnExerciseInteractionListener listener;

    public interface OnExerciseInteractionListener {
        void onEditExercise(int position);
        void onDeleteExercise(int position);
        void onSetUpdated(int exercisePosition, int setPosition, double newWeight, int newReps);
        void onSetDeleted(int exercisePosition, int setPosition);
    }

    public EditTrainingDayAdapter(List<Exercise> exercises, OnExerciseInteractionListener listener) {
        // Inizializzazione sicura: mai lasciare la lista null
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.listener = listener;
    }

    public void setExercises(List<Exercise> newExercises) {
        // Aggiorna la lista e notifica il cambiamento.
        // Se newExercises è null, usa una lista vuota per evitare crash.
        this.exercises = newExercises != null ? newExercises : new ArrayList<>();
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

        // Imposta i dati dell'esercizio
        holder.nameTextView.setText(exercise.getName());

        // Gestione espansione (Visibility e Rotazione freccia)
        boolean isExpanded = exercise.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.arrowIcon.setRotation(isExpanded ? 180f : 0f);

        // Click sull'header per espandere/collassare
        holder.headerLayout.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                boolean expanded = exercise.isExpanded();
                exercise.setExpanded(!expanded);
                notifyItemChanged(currentPos); // Aggiorna solo questo elemento per animare la freccia/layout
            }
        });

        // Click su Elimina Esercizio
        holder.btnDeleteExercise.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onDeleteExercise(currentPos);
            }
        });

        // Click su Modifica/Sostituisci Esercizio
        holder.btnEditExercise.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onEditExercise(currentPos);
            }
        });

        // Configura il RecyclerView interno (le Serie)
        holder.setupInnerRecyclerView(exercise.getSeries(), listener);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
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
        void setupInnerRecyclerView(List<Serie> series, OnExerciseInteractionListener mainListener) {
            recyclerSeries.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            // Crea il listener per l'adapter interno
            SetsAdapter.OnSetInteractionListener innerListener = new SetsAdapter.OnSetInteractionListener() {
                @Override
                public void onSetUpdated(int setPosition, double newWeight, int newReps) {
                    // Ottieni la posizione corrente dell'esercizio dinamicamente
                    int exercisePosition = getBindingAdapterPosition();
                    if (mainListener != null && exercisePosition != RecyclerView.NO_POSITION) {
                        mainListener.onSetUpdated(exercisePosition, setPosition, newWeight, newReps);
                    }
                }

                @Override
                public void onSetDeleted(int setPosition) {
                    // Ottieni la posizione corrente dell'esercizio dinamicamente
                    int exercisePosition = getBindingAdapterPosition();
                    if (mainListener != null && exercisePosition != RecyclerView.NO_POSITION) {
                        mainListener.onSetDeleted(exercisePosition, setPosition);
                    }
                }
            };

            // Crea e imposta l'adapter interno
            // Nota: Se SetsAdapter non gestisce null nel costruttore, passagli una lista vuota se series è null
            List<Serie> safeSeries = series != null ? series : new ArrayList<>();
            setsAdapter = new SetsAdapter(safeSeries, innerListener);
            recyclerSeries.setAdapter(setsAdapter);
        }
    }
}