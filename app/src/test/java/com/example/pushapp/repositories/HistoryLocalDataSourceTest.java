package com.example.pushapp.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pushapp.database.HistoryDao;
import com.example.pushapp.database.LocalDatabase;
import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link HistoryLocalDataSource}.
 * Tests local database operations for workout history.
 */
public class HistoryLocalDataSourceTest {

    @Mock
    private LocalDatabase localDatabase;

    @Mock
    private HistoryDao historyDao;

    @Mock
    private HistoryCallback mockCallback;

    private HistoryLocalDataSource dataSource;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(localDatabase.historyDao()).thenReturn(historyDao);
        dataSource = new HistoryLocalDataSource(localDatabase);
        dataSource.setHistoryCallback(mockCallback);
    }

    /**
     * Verifies that getAllHistory calls the success callback with data.
     */
    @Test
    public void getAllHistory_success_callsCallback() {
        List<HistorySessionWithExercises> historyList = new ArrayList<>();
        historyList.add(new HistorySessionWithExercises());
        when(historyDao.getAllHistory()).thenReturn(historyList);

        dataSource.getAllHistory();

        verify(mockCallback, timeout(2000)).onSuccessHistoryListFromLocal(historyList);
    }

    /**
     * Verifies that getAllHistory calls failure callback on exception.
     */
    @Test
    public void getAllHistory_failure_callsFailureCallback() {
        when(historyDao.getAllHistory()).thenThrow(new RuntimeException("DB Error"));

        dataSource.getAllHistory();

        verify(mockCallback, timeout(2000)).onFailureFromLocal(any(Exception.class));
    }

    /**
     * Verifies that saveSession calls success callback when completed.
     */
    @Test
    public void saveSession_success_callsCallback() {
        HistorySession session = new HistorySession("Test", System.currentTimeMillis(), 0);
        List<HistoryWorkoutExercise> exercises = new ArrayList<>();
        List<HistorySerie> series = new ArrayList<>();

        dataSource.saveSession(session, exercises, series, mockCallback);

        verify(mockCallback, timeout(2000)).onSuccessSaveLocal();
    }

    /**
     * Verifies that saveSession calls failure callback on exception.
     */
    @Test
    public void saveSession_failure_callsFailureCallback() {
        HistorySession session = new HistorySession("Test", System.currentTimeMillis(), 0);
        doThrow(new RuntimeException("Insert failed")).when(historyDao).insertSession(any());

        dataSource.saveSession(session, new ArrayList<>(), new ArrayList<>(), mockCallback);

        verify(mockCallback, timeout(2000)).onFailureFromLocal(any(Exception.class));
    }

    /**
     * Verifies that getGraphData returns max weight stats correctly.
     */
    @Test
    public void getGraphData_maxWeight_callsCallback() {
        List<GraphPoint> points = new ArrayList<>();
        points.add(new GraphPoint(System.currentTimeMillis(), 100f));
        when(historyDao.getMaxWeightStats(anyString())).thenReturn(points);

        dataSource.getGraphData("Bench Press", HistoryRepository.StatMetric.MAX_WEIGHT, mockCallback);

        verify(mockCallback, timeout(2000)).onSuccessGraphDataFromLocal(points);
    }

    /**
     * Verifies that getGraphData returns total volume stats correctly.
     */
    @Test
    public void getGraphData_totalVolume_callsCallback() {
        List<GraphPoint> points = new ArrayList<>();
        when(historyDao.getTotalVolumeStats(anyString())).thenReturn(points);

        dataSource.getGraphData("Squat", HistoryRepository.StatMetric.TOTAL_VOLUME, mockCallback);

        verify(mockCallback, timeout(2000)).onSuccessGraphDataFromLocal(points);
    }

    /**
     * Verifies that getGraphData returns estimated 1RM stats correctly.
     */
    @Test
    public void getGraphData_estimated1RM_callsCallback() {
        List<GraphPoint> points = new ArrayList<>();
        when(historyDao.getOneRepMaxStats(anyString())).thenReturn(points);

        dataSource.getGraphData("Deadlift", HistoryRepository.StatMetric.ESTIMATED_1RM, mockCallback);

        verify(mockCallback, timeout(2000)).onSuccessGraphDataFromLocal(points);
    }

    /**
     * Verifies that deleteSession calls the DAO delete method.
     */
    @Test
    public void deleteSession_success_callsOnSuccess() {
        Runnable onSuccess = mock(Runnable.class);

        dataSource.deleteSession("session_123", onSuccess);

        verify(historyDao, timeout(2000)).deleteSessionById("session_123");
        verify(onSuccess, timeout(2000)).run();
    }

    /**
     * Verifies that resetLocalDatabase calls deleteAllHistory.
     */
    @Test
    public void resetLocalDatabase_callsDeleteAll() {
        dataSource.resetLocalDatabase();

        verify(historyDao, timeout(2000)).deleteAllHistory();
    }
}

