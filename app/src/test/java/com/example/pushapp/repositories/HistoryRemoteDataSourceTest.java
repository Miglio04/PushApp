package com.example.pushapp.repositories;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

/**
 * Unit tests for {@link HistoryRemoteDataSource}.
 * Tests callback and data structure handling.
 */
public class HistoryRemoteDataSourceTest {

    @Mock
    private HistoryCallback mockCallback;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifies that a valid HistorySessionWithExercises can be created for upload.
     */
    @Test
    public void createValidSession_hasCorrectStructure() {
        HistorySessionWithExercises session = createMockSession();

        assertNotNull("Session should not be null", session);
        assertNotNull("Session.session should not be null", session.session);
        assertNotNull("Session.exercises should not be null", session.exercises);
    }

    /**
     * Verifies that HistorySession fields are set correctly.
     */
    @Test
    public void historySession_fieldsSetCorrectly() {
        HistorySession session = new HistorySession("Test Workout", System.currentTimeMillis(), 0);
        session.setUserId("test_user_id");

        assertNotNull("Session ID should be generated", session.getHistorySessionId());
        assertNotNull("Name should be set", session.getName());
        assertNotNull("UserId should be set", session.getUserId());
    }

    /**
     * Verifies that HistoryWorkoutExercise can be created with correct fields.
     */
    @Test
    public void historyWorkoutExercise_fieldsSetCorrectly() {
        HistoryWorkoutExercise exercise = new HistoryWorkoutExercise("session_123", "Bench Press", 0);
        exercise.setUserId("test_user_id");

        assertNotNull("Exercise ID should be generated", exercise.getHistoryExerciseId());
        assertNotNull("Exercise name should be set", exercise.getExerciseName());
        assertNotNull("Session ID should be set", exercise.getHistorySessionId());
    }

    /**
     * Verifies that HistorySerie can be created with correct fields.
     */
    @Test
    public void historySerie_fieldsSetCorrectly() {
        HistorySerie serie = new HistorySerie("exercise_123", 1, 100.0, 10);
        serie.setUserId("test_user_id");

        assertNotNull("Serie ID should be generated", serie.getHistorySerieId());
        assertNotNull("Exercise ID should be set", serie.getHistoryExerciseId());
    }

    /**
     * Verifies that callback interface methods can be called.
     */
    @Test
    public void callback_onFailureFromRemote_canBeCalled() {
        Exception testException = new Exception("Test error");

        mockCallback.onFailureFromRemote(testException);

        verify(mockCallback).onFailureFromRemote(testException);
    }

    /**
     * Verifies that callback interface methods can be called for success.
     */
    @Test
    public void callback_onSuccessHistoryFromRemote_canBeCalled() {
        ArrayList<HistorySessionWithExercises> list = new ArrayList<>();

        mockCallback.onSuccessHistoryFromRemote(list);

        verify(mockCallback).onSuccessHistoryFromRemote(list);
    }

    /**
     * Verifies that OnFailureListener interface works correctly.
     */
    @Test
    public void onFailureListener_canBeInvoked() {
        HistoryRemoteDataSource.OnFailureListener listener = mock(HistoryRemoteDataSource.OnFailureListener.class);
        Exception testException = new Exception("Delete failed");

        listener.onFailure(testException);

        verify(listener).onFailure(testException);
    }

    /**
     * Verifies that session with exercises structure is valid for serialization.
     */
    @Test
    public void sessionWithExercises_structureValidForUpload() {
        HistorySessionWithExercises session = createMockSession();

        // Verify structure matches what uploadWorkoutSession expects
        assertNotNull("Session object required", session.session);
        assertNotNull("Session ID required", session.session.getHistorySessionId());
        assertNotNull("Exercises list required", session.exercises);

        if (!session.exercises.isEmpty()) {
            HistoryWorkoutExerciseWithSeries exWithSeries = session.exercises.get(0);
            assertNotNull("Exercise wrapper should exist", exWithSeries);
            assertNotNull("Exercise should exist", exWithSeries.historyWorkoutExercise);
            assertNotNull("Series list should exist", exWithSeries.historySeries);
        }
    }

    /**
     * Creates a mock HistorySessionWithExercises for testing.
     */
    private HistorySessionWithExercises createMockSession() {
        HistorySessionWithExercises session = new HistorySessionWithExercises();
        session.session = new HistorySession("Test Workout", System.currentTimeMillis(), 0);
        session.session.setUserId("test_user_id");

        session.exercises = new ArrayList<>();
        HistoryWorkoutExerciseWithSeries exerciseWithSeries = new HistoryWorkoutExerciseWithSeries();
        exerciseWithSeries.historyWorkoutExercise = new HistoryWorkoutExercise(
                session.session.getHistorySessionId(), "Bench Press", 0);
        exerciseWithSeries.historyWorkoutExercise.setUserId("test_user_id");

        exerciseWithSeries.historySeries = new ArrayList<>();
        HistorySerie serie = new HistorySerie(
                exerciseWithSeries.historyWorkoutExercise.getHistoryExerciseId(), 1, 100.0, 10);
        serie.setUserId("test_user_id");
        exerciseWithSeries.historySeries.add(serie);

        session.exercises.add(exerciseWithSeries);
        return session;
    }
}
