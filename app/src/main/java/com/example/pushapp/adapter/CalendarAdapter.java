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

/**
 * Adapter for displaying a calendar grid or list of days in a RecyclerView.
 * Handles the rendering of individual days, highlighting the current date, selected date,
 * and indicating days with workout history.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<LocalDate> days = new ArrayList<>();
    private final Consumer<LocalDate> onDateClicked;
    private final IsWorkoutDayChecker isWorkoutDayChecker;
    private LocalDate selectedDate;
    private final LocalDate today = LocalDate.now();

    /**
     * Interface to check if a specific date contains a workout record.
     */
    public interface IsWorkoutDayChecker {
        /**
         * Checks if a workout exists for the given date.
         *
         * @param date The date to check.
         * @return True if a workout exists, false otherwise.
         */
        boolean check(LocalDate date);
    }

    /**
     * Constructs a new CalendarAdapter.
     *
     * @param onDateClicked       Callback to be invoked when a date is clicked.
     * @param isWorkoutDayChecker Helper to determine if a date has associated workouts.
     */
    public CalendarAdapter(Consumer<LocalDate> onDateClicked, IsWorkoutDayChecker isWorkoutDayChecker) {
        this.onDateClicked = onDateClicked;
        this.isWorkoutDayChecker = isWorkoutDayChecker;
    }

    /**
     * Creates a new ViewHolder for a calendar day item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type integer.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_header, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * Configures the day text, background highlights (for today/selected), and workout indicators.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the days list.
     */
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
            } else if (date.isEqual(selectedDate)) {
                holder.dayText.setBackgroundResource(R.drawable.bg_circle_outline);
                holder.dayText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_primary));
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

    /**
     * Returns the total number of items (days) in the calendar.
     *
     * @return The size of the days list.
     */
    @Override
    public int getItemCount() {
        return days.size();
    }

    /**
     * Updates the list of days and the currently selected date.
     * Refreshes the RecyclerView display.
     *
     * @param newDays         The new list of LocalDate objects representing the calendar days.
     * @param newSelectedDate The date to be highlighted as selected.
     */
    public void updateDays(List<LocalDate> newDays, LocalDate newSelectedDate) {
        this.days = newDays;
        this.selectedDate = newSelectedDate;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for caching view references for a calendar day.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dayText;
        final ImageView indicatorDot;

        /**
         * Constructs a new ViewHolder.
         *
         * @param itemView The item view.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.txtDayNumber);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
        }
    }
}
