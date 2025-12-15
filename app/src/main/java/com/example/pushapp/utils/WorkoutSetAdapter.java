package com.example.pushapp.utils;

import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;

import java.util.List;

public class WorkoutSetAdapter extends RecyclerView.Adapter<WorkoutSetAdapter.ViewHolder> {

    private List<WorkoutSet> sets;
    private OnSetCompletedListener completedListener;

    public interface OnSetCompletedListener {
        void onSetCompleted(int position, WorkoutSet set);
    }

    public WorkoutSetAdapter(List<WorkoutSet> sets) { this.sets = sets; }

    public void setOnSetCompletedListener(OnSetCompletedListener listener) {
        this.completedListener = listener;
    }

    public void setSets(List<WorkoutSet> sets) {
        this.sets = sets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutSetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_set,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutSetAdapter.ViewHolder holder, int position) {
        WorkoutSet set = sets.get(position);
        holder.setNumber.setText(String.valueOf(position + 1));

        updateCompletedUI(holder, set.isCompleted());

        if (holder.weightWatcher != null) {
            holder.weightInput.removeTextChangedListener(holder.weightWatcher);
        }
        holder.weightInput.setText(set.getWeight() == 0f ? "" : String.valueOf(set.getWeight()));
        holder.weightWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    set.setWeight(Float.parseFloat(s.toString()));
                } catch (NumberFormatException ignored) {}
            }
        };
        holder.weightInput.addTextChangedListener(holder.weightWatcher);

        if (holder.repsWatcher != null) {
            holder.repsInput.removeTextChangedListener(holder.repsWatcher);
        }
        holder.repsInput.setText(set.getReps() == 0 ? "" : String.valueOf(set.getReps()));
        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    set.setReps(Integer.parseInt(s.toString()));
                } catch (NumberFormatException ignored) {}
            }
        };
        holder.repsInput.addTextChangedListener(holder.repsWatcher);

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                sets.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, sets.size() - pos);
            }
        });

        holder.completeButton.setOnClickListener(v -> {
            if(completedListener != null) {
                completedListener.onSetCompleted(holder.getBindingAdapterPosition(), set);
            }
        });
    }

    private void updateCompletedUI(ViewHolder holder, boolean completed) {
        if (completed) {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_on_background);
            holder.weightInput.setPaintFlags(holder.weightInput.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.repsInput.setPaintFlags(holder.repsInput.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.weightInput.setEnabled(false);
            holder.repsInput.setEnabled(false);
        } else {
            holder.completeButton.setImageResource(android.R.drawable.checkbox_off_background);
            holder.weightInput.setPaintFlags(holder.weightInput.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.repsInput.setPaintFlags(holder.repsInput.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.weightInput.setEnabled(true);
            holder.repsInput.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return sets == null ? 0 : sets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber;
        final EditText weightInput;
        final EditText repsInput;
        final ImageButton deleteButton;
        final ImageButton completeButton;
        TextWatcher repsWatcher;
        TextWatcher weightWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumber = itemView.findViewById(R.id.set_index);
            weightInput = itemView.findViewById(R.id.set_weight);
            repsInput = itemView.findViewById(R.id.set_reps);
            deleteButton = itemView.findViewById(R.id.set_delete);
            completeButton = itemView.findViewById(R.id.set_complete_button);
        }
    }
}

