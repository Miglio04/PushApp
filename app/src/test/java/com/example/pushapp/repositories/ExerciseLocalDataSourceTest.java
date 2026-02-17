package com.example.pushapp.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pushapp.database.ExerciseDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ExerciseLocalDataSourceTest {

    @Mock
    private LocalDatabase localDatabase;
    @Mock
    private ExerciseDao exerciseDao;
    @Mock
    private SessionManager sessionManager;

    // We mock the Repository which implements the Callback interface
    @Mock
    private ExerciseRepository mockRepository;

    private ExerciseLocalDataSource dataSource;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(localDatabase.exerciseDao()).thenReturn(exerciseDao);

        // Remove the doAnswer for runInTransaction as it might be tricky to mock properly
        // if LocalDatabase is not fully mocked or if runInTransaction is final/static (it's an abstract method in RoomDatabase, but usually implemented by generated code).
        // Since we are mocking LocalDatabase (which is abstract), we can mock runInTransaction.
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(localDatabase).runInTransaction(any(Runnable.class));

        dataSource = new ExerciseLocalDataSource(localDatabase);
        dataSource.setCallback(mockRepository);
    }

    @Test
    public void getExercises_success_callsCallback() throws InterruptedException {
        // Arrange
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Test Exercise", "Chest", "beginner"));
        when(exerciseDao.getAllExercises()).thenReturn(exercises);

        // Act
        dataSource.getExercises();

        // Since execution is async on a static executor we can't control, we use a timeout in verification
        // verify(mockRepository, timeout(1000)).onSuccessFromLocalGet(exercises);
        // However, timeout verification is safer.
        verify(mockRepository, timeout(2000)).onSuccessFromLocalGet(exercises);
    }

    @Test
    public void getExercises_failure_callsFailureCallback() throws InterruptedException {
        // Arrange
        RuntimeException exception = new RuntimeException("DB Error");
        when(exerciseDao.getAllExercises()).thenThrow(exception);

        // Act
        dataSource.getExercises();

        // Assert
        verify(mockRepository, timeout(2000)).onFailureFromLocal(any(Exception.class));
    }

    @Test
    public void insertExercises_success_callsCallbackAndRefresh() {
        // Arrange
        ArrayList<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("New Exercise", "Back", "expert"));

        // Act
        dataSource.insertExercises(exercises);

        // Assert
        // insertExercises performs a transaction then calls getExercises
        // 1. Verify transaction stuff happened (wrapped in runInTransaction mock)
        verify(localDatabase, timeout(2000)).runInTransaction(any(Runnable.class));

        // 2. Eventually getExercises is called, which calls getAllExercises
        verify(exerciseDao, timeout(2000)).getAllExercises();
    }

    @Test
    public void deleteExercises_success_callsCallback() {
        // Act
        dataSource.deleteExercises();

        // Assert
        verify(localDatabase, timeout(2000)).runInTransaction(any(Runnable.class));
        // Inside the transaction mock (which runs immediately), deleteAll is called
        // Then onSuccessFromLocalDelete should be called
        // Note: Since runInTransaction receives a runnable and we execute it immediately in the mock,
        // the code inside the lambda runs on the test thread or the executor thread depending on how we invoke it.
        // But dataSource wraps everything in databaseWriteExecutor.execute().

        // We just need to verify the callback eventually happens
        verify(mockRepository, timeout(2000)).onSuccessFromLocalDelete();
    }
}
