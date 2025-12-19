package com.example.pushapp.utils;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.ExerciseSeries;

import java.util.List;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetViewHolder> {

    private final List<ExerciseSeries.SeriesReps> repsList;
    private final Runnable onChange;

    public SetsAdapter(List<ExerciseSeries.SeriesReps> repsList, Runnable onChange) {
        this.repsList = repsList;
        this.onChange = onChange;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_row, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        ExerciseSeries.SeriesReps set = repsList.get(position);

        holder.setNumber.setText("Set " + (position + 1));
        holder.setDetails.setText(set.getWeight() + "kg x " + set.getReps() + " reps");

        // Passiamo holder.getBindingAdapterPosition() per avere la posizione corrente aggiornata
        holder.btnEdit.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                showEditDialog(holder.itemView.getContext(), set, currentPos);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            handleDeleteSet(holder);
        });
    }

    @Override
    public int getItemCount() {
        return repsList.size();
    }

    // Aggiunto il parametro 'position'
    private void showEditDialog(android.content.Context context, ExerciseSeries.SeriesReps set, int position) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputWeight = new EditText(context);
        inputWeight.setHint("Peso (kg)");
        inputWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputWeight.setText(String.valueOf(set.getWeight()));
        layout.addView(inputWeight);

        final EditText inputReps = new EditText(context);
        inputReps.setHint("Ripetizioni");
        inputReps.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputReps.setText(String.valueOf(set.getReps()));
        layout.addView(inputReps);

        new AlertDialog.Builder(context)
                .setTitle(R.string.edit_set_title)
                .setView(layout)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    try {
                        int newWeight = Integer.parseInt(inputWeight.getText().toString());
                        int newReps = Integer.parseInt(inputReps.getText().toString());

                        set.setWeight(newWeight);
                        set.setReps(newReps);

                        notifyItemChanged(position);
                        
                        if (onChange != null) onChange.run();
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleDeleteSet(SetViewHolder holder){
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle(R.string.delete_set_title)
                .setMessage(R.string.delete_set_confirm)
                .setPositiveButton(R.string.delete, null)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber;
        final TextView setDetails;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        public SetViewHolder(View itemView) {
            super(itemView);
            setNumber = itemView.findViewById(R.id.set_number);
            setDetails = itemView.findViewById(R.id.set_details);
            btnEdit = itemView.findViewById(R.id.btn_edit_set);
            btnDelete = itemView.findViewById(R.id.btn_delete_set);
        }
    }
}
