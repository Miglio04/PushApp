package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Routine;

import java.util.List;

/**
 * Adapter for displaying a list of routine cards in a RecyclerView.
 * Manages the display of routine details and handles user interactions for starting, editing, or deleting a routine.
 */
public class RoutineCardAdapter extends RecyclerView.Adapter<RoutineCardAdapter.ViewHolder> {

    private final List<Routine> items;

    /**
     * Constructs a new RoutineCardAdapter.
     *
     * @param items The initial list of routines to display.
     */
    public RoutineCardAdapter(List<Routine> items) { this.items = items; }

    /**
     * Interface definition for a callback to be invoked when an item is clicked.
     */
    public interface OnItemClickListener { void onItemClick(Routine item); }
    private OnItemClickListener startWorkoutListener;
    private OnItemClickListener editRoutineListener;
    private OnItemClickListener deleteRoutineListener;

    /**
     * Sets the listener for the "Edit Routine" action.
     *
     * @param listener The listener callback.
     */
    public void setEditRoutineListener(OnItemClickListener listener) { this.editRoutineListener = listener; }

    /**
     * Sets the listener for the "Start Workout" action.
     *
     * @param listener The listener callback.
     */
    public void setStartWorkoutListener(OnItemClickListener listener) { this.startWorkoutListener = listener; }

    /**
     * Sets the listener for the "Delete Routine" action.
     *
     * @param listener The listener callback.
     */
    public void setDeleteRoutineListener(OnItemClickListener listener) { this.deleteRoutineListener = listener; }

    /**
     * Creates a new ViewHolder for a routine card.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type integer.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public RoutineCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routines_card,
                parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * Sets up text views and button click listeners.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull RoutineCardAdapter.ViewHolder holder, int position) {
        Routine card = items.get(position);
        holder.title.setText(card.getName());
        holder.startWorkoutButton.setOnClickListener(v -> {
            if (startWorkoutListener != null) {
                startWorkoutListener.onItemClick(card);
            }
        });
        holder.editRoutineButton.setOnClickListener(v -> {
            if (editRoutineListener != null) {
                editRoutineListener.onItemClick(card);
            }
        });
        holder.deleteRoutineButton.setOnClickListener(v -> {
            if (deleteRoutineListener != null) {
                deleteRoutineListener.onItemClick(card);
            }
        });
    }

    /**
     * Returns the total number of routine items.
     *
     * @return The size of the routines list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class for caching view references for a routine card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final Button startWorkoutButton;
        final Button editRoutineButton;
        final ImageButton deleteRoutineButton;

        /**
         * Constructs a new ViewHolder.
         *
         * @param itemView The item view.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            startWorkoutButton = itemView.findViewById(R.id.card_start_workout_button);
            editRoutineButton = itemView.findViewById(R.id.card_edit_workout_button);
            deleteRoutineButton = itemView.findViewById(R.id.delete_routine_button);
        }
    }

    /**
     * Updates the list of routine cards and refreshes the RecyclerView.
     *
     * @param newCards The new list of routines to display.
     */
    public void updateCards(List<Routine> newCards) {
        this.items.clear();
        this.items.addAll(newCards);
        notifyDataSetChanged();
    }

}
