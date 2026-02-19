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

/**
 * Adapter for displaying a history of completed workout sessions in a RecyclerView.
 * Manages the display of expandable cards showing workout details (exercises, sets, stats).
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistorySessionWithExercises> historyList;
    private final OnHistoryInteractionListener listener;
    private final SimpleDateFormat sdf;

    /**
     * Interface definition for handling interactions with history items.
     */
    public interface OnHistoryInteractionListener {
        void onDeleteClicked(HistorySessionWithExercises session);
    }

    /**
     * Constructs a new HistoryAdapter.
     *
     * @param list     The initial list of history sessions.
     * @param listener The listener for interaction events.
     */
    public HistoryAdapter(List<HistorySessionWithExercises> list, OnHistoryInteractionListener listener) {
        this.historyList = list;
        this.listener = listener;
        this.sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault());
        this.sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
    }

    /**
     * Creates a new ViewHolder for a history card.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type integer.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_card, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * Handles displaying session summaries and expanding/collapsing detailed views.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the history list.
     */
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

    /**
     * Helper method to build a formatted string string of exercises and sets for the details view.
     *
     * @param item The history session object.
     * @return A formatted string describing the completed workout.
     */
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

    /**
     * Returns the total number of history items.
     *
     * @return The size of the history list.
     */
    @Override public int getItemCount() { return historyList.size(); }

    /**
     * Updates the list of history sessions and refreshes the RecyclerView.
     *
     * @param newList The new list of history sessions.
     */
    public void updateHistory(List<HistorySessionWithExercises> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for caching view references for a history card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDetails, tvStats, tvDuration;
        View btnExpand;
        ImageView ivIcon;
        ImageView btnDelete;
        LinearLayout detailsContainer;

        /**
         * Constructs a new ViewHolder.
         *
         * @param v The item view.
         */
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