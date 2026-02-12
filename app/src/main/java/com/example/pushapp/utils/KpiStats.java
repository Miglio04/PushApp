package com.example.pushapp.utils;

public class KpiStats {
    public final int workoutsMonth;
    public final double volumeMonth;
    public final long timeMillisMonth;
    public final int currentStreak;

    public KpiStats(int workoutsMonth, double volumeMonth, long timeMillisMonth, int currentStreak) {
        this.workoutsMonth = workoutsMonth;
        this.volumeMonth = volumeMonth;
        this.timeMillisMonth = timeMillisMonth;
        this.currentStreak = currentStreak;
    }

    public int getWorkoutsMonth() {
        return workoutsMonth;
    }
    public double getVolumeMonth() {
        return volumeMonth;
    }
    public long getTimeMillisMonth() {
        return timeMillisMonth;
    }
    public int getCurrentStreak() {
        return currentStreak;
    }
}
