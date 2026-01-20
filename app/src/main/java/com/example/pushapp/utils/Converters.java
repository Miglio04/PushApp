package com.example.pushapp.utils;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Converters {
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
