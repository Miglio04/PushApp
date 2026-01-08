package com.example.pushapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.User;
import com.example.pushapp.utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Training.class, TrainingDay.class, Exercise.class, Serie.class, User.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class LocalDatabase extends RoomDatabase {
    private static volatile LocalDatabase INSTANCE;
    public abstract TrainingDao trainingDao();
    public abstract TrainingDayDao trainingDayDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SerieDao serieDao();
    public abstract UserDao userDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LocalDatabase.class, "LOCAL DATABASE")
                            .allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }

}
