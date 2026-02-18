package com.example.pushapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.User;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.utils.converters.TimeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Training.class,
        Routine.class,
        WorkoutExercise.class,
        Serie.class,
        User.class,
        HistorySession.class,
        HistoryWorkoutExercise.class,
        HistorySerie.class,
        Exercise.class
}, version = 3)
@TypeConverters({TimeConverter.class})
public abstract class LocalDatabase extends RoomDatabase {

    private static volatile LocalDatabase INSTANCE;
    public abstract TrainingDao trainingDao();
    public abstract RoutineDao routineDao();
    public abstract WorkoutExerciseDao workoutExerciseDao();
    public abstract SerieDao serieDao();
    public abstract UserDao userDao();
    public abstract HistoryDao historyDao();
    public abstract ExerciseDao exerciseDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LocalDatabase.class, "LOCAL DATABASE")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}