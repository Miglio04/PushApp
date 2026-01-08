package com.example.pushapp.utils;

import com.example.pushapp.repositories.TrainingRepository;

public class ServiceLocator {

    private static volatile ServiceLocator INSTANCE = null;

    private ServiceLocator() {};

    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator();
                }
            }
        }
        return INSTANCE;
    }

    /*public TrainingRepository getTrainingRepository() {
        return new TrainingRepository();
    }*/

}
