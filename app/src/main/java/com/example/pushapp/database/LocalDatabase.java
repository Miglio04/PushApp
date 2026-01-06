package com.example.pushapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.Training;
import com.example.pushapp.models.TrainingDay;
import com.example.pushapp.models.User;

@Database(entities = {Training.class, TrainingDay.class, Exercise.class, Serie.class, User.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract TrainingDao trainingDao();
    public abstract TrainingDayDao trainingDayDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SerieDao serieDao();
    public abstract UserDao userDao();

}
