package com.example.pushapp.utils;

import java.util.Locale;

/**
 * Data model representing Key Performance Indicators (KPIs) for workout statistics.
 * Stores monthly workout counts, volume, duration, and current streak, providing
 * helper methods for formatted display strings.
 */
public class KpiStats {
    private final int workoutsMonth;
    private final double volumeMonth;
    private final long timeMillisMonth;
    private final int currentStreak;

    /**
     * Constructs a new KpiStats instance.
     *
     * @param workoutsMonth   Number of workouts completed in the current month.
     * @param volumeMonth     Total volume lifted in the current month.
     * @param timeMillisMonth Total duration of workouts in the current month (milliseconds).
     * @param currentStreak   Current consecutive day streak of workouts.
     */
    public KpiStats(int workoutsMonth, double volumeMonth, long timeMillisMonth, int currentStreak) {
        this.workoutsMonth = workoutsMonth;
        this.volumeMonth = volumeMonth;
        this.timeMillisMonth = timeMillisMonth;
        this.currentStreak = currentStreak;
    }

    public int getWorkoutsMonth() { return workoutsMonth; }
    public double getVolumeMonth() { return volumeMonth; }
    public int getCurrentStreak() { return currentStreak; }

    /**
     * Formats the total volume into a compact string representation (e.g., "1.2K", "3.5M").
     *
     * @return Formatted volume string.
     */
    public String getFormattedVolume() {
        if (volumeMonth >= 1000000) return String.format(Locale.US, "%.1fM", volumeMonth / 1000000.0);
        if (volumeMonth >= 1000) return String.format(Locale.US, "%.1fK", volumeMonth / 1000.0);
        return String.valueOf((int) volumeMonth);
    }

    /**
     * Formats the total duration into a string representation (e.g., "1h 30m" or "45m").
     *
     * @return Formatted time string.
     */
    public String getFormattedTime() {
        long totalMinutes = timeMillisMonth / 60000;
        if (totalMinutes > 60) {
            return (totalMinutes / 60) + "h " + (totalMinutes % 60) + "m";
        }
        return totalMinutes + "m";
    }

    /**
     * Returns a motivational message based on the current streak.
     *
     * @return Motivational message string.
     */
    public String getFormattedStreakMessageText() {
        return (currentStreak > 0) ? "You're on fire! 🔥" : "Start your streak today!";
    }
}
