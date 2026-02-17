package com.example.pushapp.repositories;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link HistoryRepository}.
 * Verifies synchronization logic between local (Room) and remote (Firestore) database.
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private HistoryLocalDataSource localDataSource;

    @Mock
    private HistoryRemoteDataSource remoteDataSource;

    private HistoryRepository repository;

    @Before
    public void setUp() {
        repository = new HistoryRepository(localDataSource, remoteDataSource);
    }

    /**
     * Verifies that local save failure prevents remote upload.
     */
    @Test
    public void saveWorkoutSession_WhenLocalFails_DoesNotUploadToRemote() {
        HistorySessionWithExercises mockSession = new HistorySessionWithExercises();
        mockSession.exercises = new ArrayList<>();

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onFailureFromLocal(new Exception("Database error"));
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        repository.saveWorkoutSession(mockSession, mock(Runnable.class));

        verify(remoteDataSource, never()).uploadWorkoutSession(any(), any());
        assertThat(repository.getHistoryList().getValue(), instanceOf(Result.Error.class));
    }

    /**
     * Verifies that local save succeeds even when remote upload fails.
     */
    @Test
    public void saveWorkoutSession_WhenRemoteFails_StillCompletesLocally() {
        HistorySessionWithExercises mockSession = new HistorySessionWithExercises();
        mockSession.exercises = new ArrayList<>();
        Runnable mockCallback = mock(Runnable.class);

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onSuccessSaveLocal();
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(1);
            callback.onFailureFromRemote(new Exception("Network error"));
            return null;
        }).when(remoteDataSource).uploadWorkoutSession(any(), any());

        repository.saveWorkoutSession(mockSession, mockCallback);

        verify(localDataSource).saveSession(any(), any(), any(), any());
        verify(mockCallback).run();
    }

    /**
     * Verifies that delete calls both local and remote data sources.
     */
    @Test
    public void deleteSession_CallsBothDataSources() {
        String sessionId = "session_123";

        repository.deleteSession(sessionId);

        verify(localDataSource).deleteSession(eq(sessionId), any());
        verify(remoteDataSource).deleteSession(eq(sessionId), any());
    }

    /**
     * Verifies that fetchHistoryData calls both data sources.
     */
    @Test
    public void fetchHistoryData_CallsBothDataSources() {
        repository.fetchHistoryData();

        verify(localDataSource).getAllHistory();
        verify(remoteDataSource).fetchHistoryFromRemote();
    }

    /**
     * Verifies that local failure posts error to LiveData.
     */
    @Test
    public void onFailureFromLocal_PostsErrorToLiveData() {
        String errorMessage = "Database error";

        repository.onFailureFromLocal(new Exception(errorMessage));

        Result result = repository.getHistoryList().getValue();
        assertThat(result, instanceOf(Result.Error.class));
        assertThat(((Result.Error) result).getMessage(), equalTo(errorMessage));
    }

    /**
     * Verifies that local success posts data to LiveData.
     */
    @Test
    public void onSuccessHistoryListFromLocal_PostsDataToLiveData() {
        List<HistorySessionWithExercises> mockList = new ArrayList<>();
        mockList.add(new HistorySessionWithExercises());

        repository.onSuccessHistoryListFromLocal(mockList);

        Result result = repository.getHistoryList().getValue();
        assertThat(result, instanceOf(Result.HistorySuccess.class));
        assertThat(((Result.HistorySuccess) result).getData(), hasSize(1));
    }

    /**
     * Verifies that remote data updates local database.
     */
    @Test
    public void onSuccessHistoryFromRemote_UpdatesLocalDatabase() {
        List<HistorySessionWithExercises> remoteData = new ArrayList<>();
        remoteData.add(new HistorySessionWithExercises());

        repository.onSuccessHistoryFromRemote(remoteData);

        verify(localDataSource).updateHistoryFromRemote(remoteData);
    }

    /**
     * Verifies that empty remote list does not update local database.
     */
    @Test
    public void onSuccessHistoryFromRemote_WithEmptyList_DoesNotUpdateLocal() {
        repository.onSuccessHistoryFromRemote(new ArrayList<>());

        verify(localDataSource, never()).updateHistoryFromRemote(any());
    }

    /**
     * Verifies that createNewWorkoutSession returns null for null routine.
     */
    @Test
    public void createNewWorkoutSession_WithNullRoutine_ReturnsNull() {
        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(null);

        assertNull(result);
    }

    /**
     * Verifies that createNewWorkoutSession returns null when userId is missing.
     */
    @Test
    public void createNewWorkoutSession_WithoutUserId_ReturnsNull() {
        Routine routine = new Routine();
        routine.setName("Test");
        routine.setUserId(null);

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNull(result);
    }

    /**
     * Verifies that createNewWorkoutSession correctly maps routine fields.
     */
    @Test
    public void createNewWorkoutSession_MapsFieldsCorrectly() {
        Routine routine = new Routine();
        routine.setName("Push Day");
        routine.setUserId("user_123");

        List<WorkoutExercise> exercises = new ArrayList<>();
        WorkoutExercise ex = new WorkoutExercise("Bench Press", 0);
        List<Serie> series = new ArrayList<>();
        series.add(new Serie(1, 10, 50.0));
        series.add(new Serie(2, 8, 55.0));
        ex.setSeries(series);
        exercises.add(ex);
        routine.setWorkoutExercises(exercises);

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull(result);
        assertThat(result.session.getName(), equalTo("Push Day"));
        assertThat(result.session.getUserId(), equalTo("user_123"));
        assertThat(result.exercises, hasSize(1));
        assertThat(result.exercises.get(0).historySeries, hasSize(2));
    }

    /**
     * Verifies that each session gets a unique ID.
     */
    @Test
    public void createNewWorkoutSession_GeneratesUniqueIds() {
        Routine routine = new Routine();
        routine.setName("Workout");
        routine.setUserId("user_123");
        routine.setWorkoutExercises(new ArrayList<>());

        HistorySessionWithExercises session1 = repository.createNewWorkoutSessionWithoutTemplate(routine);
        HistorySessionWithExercises session2 = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotEquals(session1.session.getHistorySessionId(), session2.session.getHistorySessionId());
    }
}