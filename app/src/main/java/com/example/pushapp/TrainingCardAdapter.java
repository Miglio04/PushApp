package com.example.pushapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrainingCardAdapter extends RecyclerView.Adapter<TrainingCardAdapter.ViewHolder> {

    private final List<TrainingCard> items;

    public TrainingCardAdapter(List<TrainingCard> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TrainingCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training_card,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingCardAdapter.ViewHolder holder, int position) {
        TrainingCard card = items.get(position);
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
        }
    }
}

