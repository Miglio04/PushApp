package com.example.pushapp.utils.converters;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

public class TimeConverter {
    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        if (value == null) return null;
        return new Timestamp(value / 1000, (int) ((value % 1000) * 1000000));
    }

    @TypeConverter
    public static Long dateToTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toDate().getTime();
    }
}
