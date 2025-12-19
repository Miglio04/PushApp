package com.example.pushapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.ExerciseSeries;

import java.util.List;

public class EditTrainingDayAdapter extends RecyclerView.Adapter<EditTrainingDayAdapter.ViewHolder> {

    private final List<ExerciseUiModel> items;

    public EditTrainingDayAdapter(List<ExerciseUiModel> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTextView;
        public final ImageView arrowIcon;
        public final LinearLayout headerLayout;
        public final LinearLayout expandableLayout;
        public final RecyclerView setsRecyclerView;
        public final ImageButton btnEditExercise;
        public final ImageButton btnDeleteExercise;

        public ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.exercise_name);
            arrowIcon = view.findViewById(R.id.arrow_icon);
            headerLayout = view.findViewById(R.id.header_layout);
            expandableLayout = view.findViewById(R.id.expandable_layout);
            setsRecyclerView = view.findViewById(R.id.sets_recycler_view);
            btnEditExercise = view.findViewById(R.id.btn_edit_exercise);
            btnDeleteExercise = view.findViewById(R.id.btn_delete_exercise);
        }
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
        ExerciseUiModel item = items.get(position);
        ExerciseSeries series = item.getExerciseSeries();

        holder.nameTextView.setText(series.getExercise().getName());

        boolean isExpanded = item.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.arrowIcon.setRotation(isExpanded ? 180f : 0f);

        holder.btnDeleteExercise.setOnClickListener(v -> handleDeleteExercise(holder));

        // Listener Modifica Esercizio (Cambia tipo)
        holder.btnEditExercise.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                showChangeExerciseDialog(holder.itemView.getContext(), series, currentPos);
            }
        });

        // Gestione espansione click header
        holder.headerLayout.setOnClickListener(v -> {
            boolean newState = !item.isExpanded();
            item.setExpanded(newState);
            notifyItemChanged(holder.getBindingAdapterPosition());
        });

        // Configurazione RecyclerView annidata per i Set
        setupSetsRecyclerView(holder, series);
    }

    private void setupSetsRecyclerView(ViewHolder holder, ExerciseSeries series) {
        holder.setsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        SetsAdapter setsAdapter = new SetsAdapter(series.getReps(), () -> {
            // Callback opzionale
        });
        holder.setsRecyclerView.setAdapter(setsAdapter);
    }

    private void handleDeleteExercise(ViewHolder holder) {
        int position = holder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        ExerciseUiModel item = items.get(position);
        String exerciseName = item.getExerciseSeries().getExercise().getName();

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Elimina Esercizio")
                .setMessage("Sei sicuro di voler eliminare " + exerciseName + "?")
                .setPositiveButton("Elimina", null)
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void showChangeExerciseDialog(Context context, ExerciseSeries series, int position) {
        Exercise[] availableExercises = TrainingListGenerator.generateExercises();
        
        String[] exerciseNames = new String[availableExercises.length];
        int selectedIndex = 0;
        for (int i = 0; i < availableExercises.length; i++) {
            exerciseNames[i] = availableExercises[i].getName();
            if (availableExercises[i].getId() == series.getExercise().getId() || 
                availableExercises[i].getName().equals(series.getExercise().getName())) {
                selectedIndex = i;
            }
        }

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final Spinner spinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, exerciseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        
        layout.addView(spinner);

        new AlertDialog.Builder(context)
                .setTitle("Cambia Esercizio")
                .setView(layout)
                .setPositiveButton("Salva", (dialog, which) -> {
                    int newIndex = spinner.getSelectedItemPosition();
                    Exercise newExercise = availableExercises[newIndex];
                    series.setExercise(newExercise);
                    notifyItemChanged(position);
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
