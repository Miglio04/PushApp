package com.example.pushapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;

import java.util.List;

public class TrainingDaysCardAdapter extends RecyclerView.Adapter<TrainingDaysCardAdapter.ViewHolder> {

    private final List<TrainingDaysCard> items;
    public TrainingDaysCardAdapter(List<TrainingDaysCard> items) { this.items = items; }

    public interface OnItemClickListener { void onItemClick(TrainingDaysCard item); }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    @NonNull
    @Override
    public TrainingDaysCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training_days_card,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingDaysCardAdapter.ViewHolder holder, int position) {
        TrainingDaysCard card = items.get(position);
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());
        holder.startWorkoutButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(card);
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            startWorkoutButton = itemView.findViewById(R.id.card_start_workout_button);
        }
    }
}

