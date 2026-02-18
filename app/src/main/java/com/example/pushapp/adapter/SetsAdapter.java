package com.example.pushapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Serie;
import com.google.android.material.textfield.TextInputEditText;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_set, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        etWeight.setText(String.valueOf(serie.getTargetWeight()));
        etReps.setText(String.valueOf(serie.getTargetReps()));

        btnSave.setOnClickListener(v -> {
            try {
                String weightStr = etWeight.getText() != null ? etWeight.getText().toString() : "";
                String repsStr = etReps.getText() != null ? etReps.getText().toString() : "";

                double newWeight = Double.parseDouble(weightStr);
                int newReps = Integer.parseInt(repsStr);

                if (listener != null) {
                    listener.onSetUpdated(position, newWeight, newReps);
                }
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(context, context.getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
