package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_sessions")
public class HistorySession {

    @PrimaryKey
    @NonNull
    public String sessionId;

    public String name;
    public long startTime;
    public long endTime;
    public long duration;

    public HistorySession() {}

    public HistorySession(@NonNull String sessionId, String name, long startTime, long endTime) {
        this.sessionId = sessionId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
    }

    @NonNull
    public String getSessionId() { return sessionId; }
    public void setSessionId(@NonNull String sessionId) { this.sessionId = sessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}