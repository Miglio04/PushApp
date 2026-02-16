package com.example.pushapp.utils;

import java.util.Locale;

public class KpiStats {
    private final int workoutsMonth;
    private final double volumeMonth;
    private final long timeMillisMonth;
    private final int currentStreak;

    public KpiStats(int workoutsMonth, double volumeMonth, long timeMillisMonth, int currentStreak) {
        this.workoutsMonth = workoutsMonth;
        this.volumeMonth = volumeMonth;
        this.timeMillisMonth = timeMillisMonth;
        this.currentStreak = currentStreak;
    }

    public int getWorkoutsMonth() { return workoutsMonth; }
    public double getVolumeMonth() { return volumeMonth; }
    public long getTimeMillisMonth() { return timeMillisMonth; }
    public int getCurrentStreak() { return currentStreak; }

    public String getFormattedVolume() {
        if (volumeMonth >= 1000000) return String.format(Locale.US, "%.1fM", volumeMonth / 1000000.0);
        if (volumeMonth >= 1000) return String.format(Locale.US, "%.1fK", volumeMonth / 1000.0);
        return String.valueOf((int) volumeMonth);
    }

    public String getFormattedTime() {
        long totalMinutes = timeMillisMonth / 60000;
        if (totalMinutes > 60) {
            return (totalMinutes / 60) + "h " + (totalMinutes % 60) + "m";
        }
        return totalMinutes + "m";
    }

    public String getFormattedStreakCountText() {
        return String.format(Locale.ENGLISH, "%d %s", currentStreak, (currentStreak == 1 ? "DAY STREAK!" : "DAYS STREAK!"));
    }

    public String getFormattedStreakMessageText() {
        return (currentStreak > 0) ? "You're on fire! 🔥" : "Start your streak today!";
    }
}
    