package com.example.pushapp.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;

import java.util.List;

public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.ViewHolder> {
    
    private List<WorkoutCard> cards;

    // Valutare quali modifiche si vogliono implementare e di conseguenza se aggiungere altri metodi a questa interfaccia
    public interface OnCardInteractionListener {
        void onNoteChanged(int cardPosition, String newNote);
        void onRestTimeChanged(int cardPosition, int newRestTime);
        void onAddSetClicked(int cardPosition);
        void onSetCompleted(int cardPosition, int setPosition);
    }

    private final OnCardInteractionListener listener;

    public WorkoutCardAdapter(List<WorkoutCard> cards, OnCardInteractionListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    public void setCards(List<WorkoutCard> newCards) {
        this.cards = newCards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_card,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutCardAdapter.ViewHolder holder, int position) {
        WorkoutCard card = cards.get(position);
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());
        holder.image.setImageResource(card.getImageResId());

        // Setup note EditText with TextWatcher
        if (holder.noteWatcher != null) {
            holder.note.removeTextChangedListener(holder.noteWatcher);
        }
        holder.note.setText(card.getNote());
        holder.noteWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                listener.onNoteChanged(holder.getBindingAdapterPosition(), s.toString());
            }
        };
        holder.note.addTextChangedListener(holder.noteWatcher);

        // Setup rest time spinner
        setupRestTimeSpinner(holder, card);

        // Setup sets RecyclerView
        if(holder.setsAdapter == null) {
            holder.setsAdapter = new WorkoutSetAdapter(card.getSets());
            holder.setsRecycler.setAdapter(holder.setsAdapter);
        } else {
            holder.setsAdapter.setSets(card.getSets());
        }
        holder.setsAdapter.setOnSetCompletedListener((setPosition, set) -> {
            listener.onSetCompleted(holder.getBindingAdapterPosition(), setPosition);
        });

        // Add set
        holder.addSetButton.setOnClickListener(v -> {
            listener.onAddSetClicked(holder.getBindingAdapterPosition());
        });

    }

    private void setupRestTimeSpinner(ViewHolder holder, WorkoutCard card) {
        String[] restOptions = {"30s", "45s", "60s", "90s", "120s", "180s"};
        int[] restValues = {30, 45, 60, 90, 120, 180};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                restOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.restTimeSpinner.setAdapter(adapter);

        holder.restTimeSpinner.setOnItemSelectedListener(null);

        // Set current selection
        int currentRest = card.getRestTimeSeconds();
        for (int i = 0; i < restValues.length; i++) {
            if (restValues[i] == currentRest) {
                holder.restTimeSpinner.setSelection(i);
                break;
            }
        }

        holder.restTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int newRestTime = restValues[pos];
                if (card.getRestTimeSeconds() != newRestTime) {
                    listener.onRestTimeChanged(holder.getBindingAdapterPosition(), newRestTime);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final ImageView image;
        final EditText note;
        final RecyclerView setsRecycler;
        final Button addSetButton;
        final Spinner restTimeSpinner;

        TextWatcher noteWatcher;
        WorkoutSetAdapter setsAdapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            image = itemView.findViewById(R.id.card_image);
            note = itemView.findViewById(R.id.card_note);
            setsRecycler = itemView.findViewById(R.id.card_sets_recycler);
            addSetButton = itemView.findViewById(R.id.card_add_set);
            restTimeSpinner = itemView.findViewById(R.id.card_rest_spinner);
            setsRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.VERTICAL, false));
        }
    }
}