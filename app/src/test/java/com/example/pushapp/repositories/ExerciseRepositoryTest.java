package com.example.pushapp.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.utils.Constants;
import com.example.pushapp.utils.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class ExerciseRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ExerciseLocalDataSource localDataSource;

    @Mock
    private ExerciseAPIDataSource apiDataSource;

    @Mock
    private ExerciseSampleDataSource sampleDataSource;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Observer<Result> observer;

    private MockedStatic<Log> mockedLog;

    private ExerciseRepository repository;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockedLog = mockStatic(Log.class);
        mockedLog.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
        mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

        repository = new ExerciseRepository(localDataSource, apiDataSource, sampleDataSource, sessionManager);
        repository.getExercises().observeForever(observer);
    }

    @After
    public void tearDown() {
        if (mockedLog != null) {
            mockedLog.close();
        }
    }

    @Test
    public void fetchExercises_whenCacheExpired_callsApi() {
        // Arrange
        when(sessionManager.getLastApiFetchTime()).thenReturn(0L); // Expired/Never fetched

        // Act
        repository.fetchExercises();

        // Assert
        verify(apiDataSource).fetchAllExercises();
        verify(localDataSource, never()).getExercises();
    }

    @Test
    public void fetchExercises_whenCacheValid_callsLocal() {
        // Arrange
        long recentTime = System.currentTimeMillis();
        when(sessionManager.getLastApiFetchTime()).thenReturn(recentTime); // Valid

        // Act
        repository.fetchExercises();

        // Assert
        verify(localDataSource).getExercises();
        verify(apiDataSource, never()).fetchAllExercises();
    }

    @Test
    public void onSuccessFromRemote_updatesLiveDataAndSavesToLocal() {
        // Arrange
        ArrayList<Exercise> exercises = new ArrayList<>();
        Exercise ex = new Exercise("Push Up", "Chest", "beginner");
        exercises.add(ex);

        // Act
        repository.onSuccessFromRemote(exercises);

        // Assert
        // Verify LiveData updated
        verify(observer).onChanged(argThat(result ->
            result instanceof Result.ExerciseSuccess &&
            ((Result.ExerciseSuccess) result).getData().size() == 1
        ));

        // Verify saved to local DB
        verify(localDataSource).insertExercises(exercises);
    }

    @Test
    public void onFailureFromRemote_triesLocalFallback() {
        // Act
        repository.onFailureFromRemote(new Exception("Network error"));

        // Assert
        verify(localDataSource).getExercises();
    }
}
