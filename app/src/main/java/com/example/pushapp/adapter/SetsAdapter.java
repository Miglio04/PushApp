package com.example.pushapp.adapter;

import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Serie;

import java.util.List;

/**
 * Adapter for managing and displaying a list of workout sets (series) in a RecyclerView.
 * Handles user interactions for updating or deleting sets via dialogs.
 */
public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetViewHolder> {
    private List<Serie> series;

    /**
     * Interface for handling set modifications.
     */
    public interface OnSetInteractionListener {
        void onSetUpdated(int position, double newWeight, int newReps);
        void onSetDeleted(int position);
    }

    private final OnSetInteractionListener listener;

    /**
     * Constructs a new SetsAdapter.
     *
     * @param series   The list of sets to display.
     * @param listener The listener for set interaction events.
     */
    public SetsAdapter(List<Serie> series, OnSetInteractionListener listener) {
        this.series = series;
        this.listener = listener;
    }

    /**
     * Updates the list of series and refreshing the view.
     *
     * @param newSeries The new list of series.
     */
    public void setSeries(List<Serie> newSeries) {
        this.series = newSeries;
        notifyDataSetChanged();
    }

    /**
     * Creates a new SetViewHolder.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type.
     * @return A new SetViewHolder instance.
     */
    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_row, parent, false);
        return new SetViewHolder(view);
    }

    /**
     * Binds data to the SetViewHolder at the specified position.
     * Sets up click listeners for edit and delete actions.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Serie serie = series.get(position);
        int displayPosition = position + 1;

        holder.setNumber.setText("Set " + displayPosition);
        holder.setDetails.setText(serie.getTargetWeight() + "kg x " + serie.getTargetReps() + " reps");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    showEditDialog(holder.itemView.getContext(), series.get(currentPos), currentPos);
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    listener.onSetDeleted(currentPos);
                }
            }
        });
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
     * Displays a dialog to edit the weight and reps of a specific set.
     *
     * @param context  The context to display the dialog in.
     * @param serie    The set being edited.
     * @param position The position of the set in the adapter.
     */
    private void showEditDialog(Context context, Serie serie, int position) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputWeight = new EditText(context);
        inputWeight.setHint("Peso (kg)");
        inputWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputWeight.setText(String.valueOf(serie.getTargetWeight())); // Usa getTargetWeight
        layout.addView(inputWeight);

        final EditText inputReps = new EditText(context);
        inputReps.setHint("Ripetizioni");
        inputReps.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputReps.setText(String.valueOf(serie.getTargetReps())); // Usa getTargetReps
        layout.addView(inputReps);

        new AlertDialog.Builder(context)
                .setTitle("Modifica Serie")
                .setView(layout)
                .setPositiveButton("Conferma", (dialog, which) -> {
                    try {
                        double newWeight = Double.parseDouble(inputWeight.getText().toString());
                        int newReps = Integer.parseInt(inputReps.getText().toString());

                        if (listener != null) {
                            listener.onSetUpdated(position, newWeight, newReps);
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Input non valido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    /**
     * ViewHolder class for caching view references for a set item.
     */
    public static class SetViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber;
        final TextView setDetails;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        /**
         * Constructs a new SetViewHolder.
         *
         * @param itemView The item view.
         */
        public SetViewHolder(View itemView) {
            super(itemView);
            setNumber = itemView.findViewById(R.id.set_number);
            setDetails = itemView.findViewById(R.id.set_details);
            btnEdit = itemView.findViewById(R.id.btn_edit_set);
            btnDelete = itemView.findViewById(R.id.btn_delete_set);
        }
    }
}
