package com.example.pushapp.models.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "historySessions")
public class HistorySession {

    @PrimaryKey
    @ColumnInfo(name = "historySessionId")
    @NonNull
    public String historySessionId;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "startTime")

    public long startTime;
    @ColumnInfo(name = "endTime")

    public long endTime;
    @ColumnInfo(name = "duration")

    public long duration;

    public HistorySession() {
        this.historySessionId = UUID.randomUUID().toString();
    }

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
}