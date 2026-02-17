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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistorySessionWithExercises> historyList;
    private final OnHistoryInteractionListener listener;
    private final SimpleDateFormat sdf;

    public interface OnHistoryInteractionListener {
        void onDeleteClicked(HistorySessionWithExercises session);
    }

    public HistoryAdapter(List<HistorySessionWithExercises> list, OnHistoryInteractionListener listener) {
        this.historyList = list;
        this.listener = listener;
        this.sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.ENGLISH);
        this.sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
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
        holder.tvName.setText(item.session.getName());

        holder.tvDate.setText(sdf.format(new Date(item.session.getStartTime())));

        int exerciseCount = item.exercises != null ? item.exercises.size() : 0;
        holder.tvStats.setText(exerciseCount + (exerciseCount == 1 ? " exercise" : " exercises"));

        long durationMillis = item.session.getDuration();
        holder.tvDuration.setText(formatDuration(durationMillis));

        holder.tvDetails.setText(buildDetailsString(item));

        holder.btnExpand.setOnClickListener(v -> {
            boolean visible = holder.detailsContainer.getVisibility() == View.VISIBLE;
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView.getParent());
            holder.detailsContainer.setVisibility(visible ? View.GONE : View.VISIBLE);
            holder.ivIcon.setRotation(visible ? 90 : 270);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    private String formatDuration(long millis) {
        if (millis <= 0) return "0 min";
        long totalMinutes = millis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + " min";
    }

    @NonNull
    private String buildDetailsString(HistorySessionWithExercises item) {
        StringBuilder sb = new StringBuilder();
        if (item.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries ex : item.exercises) {
                sb.append("• ").append(ex.historyWorkoutExercise.getExerciseName()).append("\n");
                if (ex.historySeries != null) {
                    for (HistorySerie s : ex.historySeries) {
                        sb.append("  Set ").append(s.getSetNumber()).append(": ").append(s.getWeight()).append("kg x ").append(s.getReps()).append("\n");
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    @Override public int getItemCount() { return historyList.size(); }

    public void updateHistory(List<HistorySessionWithExercises> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDetails, tvStats, tvDuration;
        View btnExpand;
        ImageView ivIcon;
        ImageView btnDelete;
        LinearLayout detailsContainer;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_workout_name);
            tvDate = v.findViewById(R.id.tv_workout_date);
            tvDetails = v.findViewById(R.id.tv_exercises_details);
            tvStats = v.findViewById(R.id.tv_workout_stats);
            tvDuration = v.findViewById(R.id.tv_workout_duration);
            btnExpand = v.findViewById(R.id.workout_icon_bg);
            ivIcon = v.findViewById(R.id.iv_expand_icon);
            detailsContainer = v.findViewById(R.id.details_container);
            btnDelete = v.findViewById(R.id.btn_delete_workout);
        }
    }
}