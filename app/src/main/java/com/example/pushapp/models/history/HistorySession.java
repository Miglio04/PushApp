package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Entity representing a completed workout session in history.
 * Stores summary data like start/end time, duration, and name.
 */
@Entity(tableName = "historySessions")
public class HistorySession {

    @PrimaryKey
    @ColumnInfo(name = "historySessionId")
    @NonNull
    private String historySessionId;
    @ColumnInfo(name = "userId")
    private String userId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "startTime")
    private long startTime;
    @ColumnInfo(name = "endTime")
    private long endTime;
    @ColumnInfo(name = "duration")
    private long duration;

    /**
     * Default constructor.
     */
    public HistorySession() {
        this.historySessionId = UUID.randomUUID().toString();
    }

    /**
     * Constructs a HistorySession.
     *
     * @param name      The name of the session.
     * @param startTime The start timestamp in milliseconds.
     * @param endTime   The end timestamp in milliseconds.
     */
    public HistorySession(String name, long startTime, long endTime) {
        this.historySessionId = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
    }

    @NonNull
    public String getHistorySessionId() { return historySessionId; }
    public void setHistorySessionId(@NonNull String historySessionId) { this.historySessionId = historySessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}