package com.example.pushapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<LocalDate> days = new ArrayList<>();
    private final Consumer<LocalDate> onDateClicked;
    private final IsWorkoutDayChecker isWorkoutDayChecker;
    private final LocalDate today = LocalDate.now();

    public interface IsWorkoutDayChecker {
        boolean check(LocalDate date);
    }

    public CalendarAdapter(Consumer<LocalDate> onDateClicked, IsWorkoutDayChecker isWorkoutDayChecker) {
        this.onDateClicked = onDateClicked;
        this.isWorkoutDayChecker = isWorkoutDayChecker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate date = days.get(position);

        if (date == null) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.dayText.setText(String.valueOf(date.getDayOfMonth()));

            if (date.isEqual(today)) {
                holder.dayText.setBackgroundResource(R.drawable.bg_circle_selection);
                holder.dayText.setTextColor(Color.WHITE);
            } else {
                holder.dayText.setBackground(null);
                holder.dayText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_onSurface));
            }

            boolean isWorkoutDay = isWorkoutDayChecker.check(date);
            holder.indicatorDot.setVisibility(isWorkoutDay ? View.VISIBLE : View.INVISIBLE);
            holder.indicatorDot.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_primary));

            holder.itemView.setOnClickListener(v -> onDateClicked.accept(date));
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<LocalDate> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dayText;
        final ImageView indicatorDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.txtDayNumber);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
        }
    }
}
    