package com.example.pushapp.viewModels;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.Result;
import com.example.pushapp.repositories.ExerciseRepository;
import com.example.pushapp.repositories.TrainingRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class TrainingViewModelUnitTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private Observer<Result> trainingsObserver;

    @Mock
    private Observer<Result> exercisesObserver;

    private TrainingViewModel trainingViewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Configure mock repositories to return Main LiveData
        when(trainingRepository.getTrainingList()).thenReturn(new MutableLiveData<>());
        when(exerciseRepository.getExercises()).thenReturn(new MutableLiveData<>());

        trainingViewModel = new TrainingViewModel(trainingRepository, exerciseRepository);
    }

    @Test
    public void fetchTrainings_callsRepository() {
        // Arrange
        String userId = "testUser123";
        trainingViewModel.getTrainings().observeForever(trainingsObserver);

        // Act
        trainingViewModel.fetchTrainings(userId);

        // Assert
        verify(trainingRepository).fetchTrainings(userId);
    }

    @Test
    public void loadAvailableExercises_success_updatesFilteredList() {
        // Arrange
        // Setup mock response from repository LiveData
        MutableLiveData<Result> repoExercisesLiveData = new MutableLiveData<>();
        when(exerciseRepository.getExercises()).thenReturn(repoExercisesLiveData);

        // Re-init ViewModel to bind to the new Mock LiveData
        trainingViewModel = new TrainingViewModel(trainingRepository, exerciseRepository);

        trainingViewModel.getFilteredAvailableExercises().observeForever(exercisesObserver);

        // Create dummy data
        List<Exercise> exerciseList = new ArrayList<>();
        Exercise ex1 = new Exercise("Push Up", "Chest", "beginner");
        exerciseList.add(ex1);

        Result.ExerciseSuccess successResult = new Result.ExerciseSuccess(exerciseList);

        // Act
        // 1. Trigger fetch
        trainingViewModel.loadAvailableExercises();
        verify(exerciseRepository).fetchExercises();

        // 2. Simulate repository updating the LiveData
        repoExercisesLiveData.setValue(successResult);

        // Assert
        // Verify that the ViewModel's mediator updates the observer
        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(exercisesObserver, atLeastOnce()).onChanged(captor.capture());

        Result result = captor.getValue();
        assertTrue(result instanceof Result.ExerciseSuccess);
        assertEquals(1, ((Result.ExerciseSuccess)result).getData().size());
        assertEquals("Push Up", ((Result.ExerciseSuccess)result).getData().get(0).getName());
    }

    @Test
    public void applyFilters_filtersCorrectly() {
        // Arrange
        MutableLiveData<Result> repoExercisesLiveData = new MutableLiveData<>();
        when(exerciseRepository.getExercises()).thenReturn(repoExercisesLiveData);
        trainingViewModel = new TrainingViewModel(trainingRepository, exerciseRepository);
        trainingViewModel.getFilteredAvailableExercises().observeForever(exercisesObserver);

        // Populate initial list
        List<Exercise> exerciseList = new ArrayList<>();

        Exercise ex1 = new Exercise("Push Up", "Chest", "beginner"); // Muscle Chest
        exerciseList.add(ex1);

        Exercise ex2 = new Exercise("Squat", "Legs", "intermediate"); // Muscle Legs
        exerciseList.add(ex2);

        repoExercisesLiveData.setValue(new Result.ExerciseSuccess(exerciseList));

        // Act - Apply Filter for "Chest"
        trainingViewModel.applyFilters("", "Chest", "Tutti");

        // Assert
        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(exercisesObserver, atLeastOnce()).onChanged(captor.capture());

        Result lastResult = captor.getValue();
        assertTrue(lastResult instanceof Result.ExerciseSuccess);
        List<Exercise> filtered = ((Result.ExerciseSuccess) lastResult).getData();

        assertEquals(1, filtered.size());
        assertEquals("Push Up", filtered.get(0).getName());
    }
}

