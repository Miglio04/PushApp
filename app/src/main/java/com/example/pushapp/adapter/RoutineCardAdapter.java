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

public class RoutineCardAdapter extends RecyclerView.Adapter<RoutineCardAdapter.ViewHolder> {

    private final List<Routine> items;
    public RoutineCardAdapter(List<Routine> items) { this.items = items; }

    public interface OnItemClickListener { void onItemClick(Routine item); }
    private OnItemClickListener startWorkoutListener;
    private OnItemClickListener editRoutineListener;
    private OnItemClickListener deleteRoutineListener;

    public void setEditRoutineListener(OnItemClickListener listener) { this.editRoutineListener = listener; }

    public void setStartWorkoutListener(OnItemClickListener listener) { this.startWorkoutListener = listener; }

    public void setDeleteRoutineListener(OnItemClickListener listener) { this.deleteRoutineListener = listener; }

    @NonNull
    @Override
    public RoutineCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routines_card,
                parent, false);
        return new ViewHolder(v);
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final Button startWorkoutButton;
        final Button editRoutineButton;
        final ImageButton deleteRoutineButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            startWorkoutButton = itemView.findViewById(R.id.card_start_workout_button);
            editRoutineButton = itemView.findViewById(R.id.card_edit_workout_button);
            deleteRoutineButton = itemView.findViewById(R.id.delete_routine_button);
        }
    }

    public void updateCards(List<Routine> newCards) {
        this.items.clear();
        this.items.addAll(newCards);
        notifyDataSetChanged();
    }

}

