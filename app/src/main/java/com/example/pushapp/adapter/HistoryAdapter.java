package com.example.pushapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import com.example.pushapp.R;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.models.history.HistorySerie;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistorySessionWithExercises> historyList;
    private OnHistoryInteractionListener listener;

    public interface OnHistoryInteractionListener {
        void onHistoryClicked(HistorySessionWithExercises session);
        void onDeleteClicked(HistorySessionWithExercises session);
    }

    public HistoryAdapter(List<HistorySessionWithExercises> list, OnHistoryInteractionListener listener) {
        this.historyList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorySessionWithExercises item = historyList.get(position);
        holder.tvName.setText(item.session.name);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.ITALY);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        holder.tvDate.setText(sdf.format(new Date(item.session.startTime)));

        StringBuilder sb = new StringBuilder();
        if (item.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries ex : item.exercises) {
                sb.append("• ").append(ex.historyWorkoutExercise.exerciseName).append("\n");
                if (ex.historySeries != null) {
                    for (HistorySerie s : ex.historySeries) {
                        sb.append("  Set ").append(s.setNumber).append(": ").append(s.weight).append("kg x ").append(s.reps).append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        holder.tvDetails.setText(sb.toString().trim());

        holder.btnExpand.setOnClickListener(v -> {
            boolean visible = holder.detailsContainer.getVisibility() == View.VISIBLE;
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView.getParent());
            holder.detailsContainer.setVisibility(visible ? View.GONE : View.VISIBLE);
            holder.ivIcon.setRotation(visible ? 90 : 270);
            listener.onHistoryClicked(item);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override public int getItemCount() { return historyList.size(); }

    public void updateHistory(List<HistorySessionWithExercises> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDetails;
        View btnExpand;
        ImageView ivIcon;
        LinearLayout detailsContainer;
        MaterialButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_workout_name);
            tvDate = v.findViewById(R.id.tv_workout_date);
            tvDetails = v.findViewById(R.id.tv_exercises_details);
            btnExpand = v.findViewById(R.id.workout_icon_bg);
            ivIcon = v.findViewById(R.id.iv_expand_icon);
            detailsContainer = v.findViewById(R.id.details_container);
            btnDelete = v.findViewById(R.id.btn_delete_workout);
        }
    }
}