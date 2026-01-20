package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.ui.main.graphicComponents.RoutinesCard;

import java.util.List;

public class RoutineCardAdapter extends RecyclerView.Adapter<RoutineCardAdapter.ViewHolder> {

    private final List<RoutinesCard> items;
    public RoutineCardAdapter(List<RoutinesCard> items) { this.items = items; }

    public interface OnItemClickListener { void onItemClick(RoutinesCard item); }
    private OnItemClickListener startWorkoutListener;

    private OnItemClickListener editWorkoutListener;
    public void setEditWorkoutListener(OnItemClickListener listener) { this.editWorkoutListener = listener; }

    public void setStartWorkoutListener(OnItemClickListener listener) { this.startWorkoutListener = listener; }

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
        RoutinesCard card = items.get(position);
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());
        holder.startWorkoutButton.setOnClickListener(v -> {
            if (startWorkoutListener != null) {
                startWorkoutListener.onItemClick(card);
            }
        });
        holder.editWorkoutButton.setOnClickListener(v -> {
            if (editWorkoutListener != null) {
                editWorkoutListener.onItemClick(card);
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
        final Button editWorkoutButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            startWorkoutButton = itemView.findViewById(R.id.card_start_workout_button);
            editWorkoutButton = itemView.findViewById(R.id.card_edit_workout_button);
        }
    }

    public void updateCards(List<RoutinesCard> newCards) {
        this.items.clear();
        this.items.addAll(newCards);
        notifyDataSetChanged();
    }

}

