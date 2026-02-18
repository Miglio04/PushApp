package com.example.pushapp.viewModels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.repositories.HistoryRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for HistoryViewModel filter functionality.
 * Tests ALL, THIS_WEEK, and THIS_MONTH filters.
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryViewModelFilterTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private HistoryRepository repository;

    private MutableLiveData<Result> historyListLiveData;
    private HistoryViewModel viewModel;

    @Before
    public void setup() {
        historyListLiveData = new MutableLiveData<>();
        when(repository.getHistoryList()).thenReturn(historyListLiveData);
        viewModel = new HistoryViewModel(repository);
    }

    /**
     * Verifies that ALL filter returns all sessions.
     */
    @Test
    public void filterByPeriod_ALL_returnsAllSessions() {
        // Setup: 3 sessions at different times
        List<HistorySessionWithExercises> sessions = createTestSessions();
        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        // Act
        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);

        // Assert
        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    /**
     * Verifies that THIS_WEEK filter returns only sessions from current week.
     */
    @Test
    public void filterByPeriod_THIS_WEEK_returnsOnlyThisWeekSessions() {
        List<HistorySessionWithExercises> sessions = createTestSessions();
        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_WEEK);

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        // Should contain only today's session
        assertTrue(result.size() <= 2); // Today and possibly yesterday if same week

        // Verify all returned sessions are from this week
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        long startOfWeekMillis = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        for (HistorySessionWithExercises session : result) {
            assertTrue("Session should be from this week",
                session.session.getStartTime() >= startOfWeekMillis);
        }
    }

    /**
     * Verifies that THIS_MONTH filter returns only sessions from current month.
     */
    @Test
    public void filterByPeriod_THIS_MONTH_returnsOnlyThisMonthSessions() {
        List<HistorySessionWithExercises> sessions = createTestSessions();
        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_MONTH);

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);

        // Verify all returned sessions are from this month
        LocalDate now = LocalDate.now();
        long startOfMonthMillis = now.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        for (HistorySessionWithExercises session : result) {
            assertTrue("Session should be from this month",
                session.session.getStartTime() >= startOfMonthMillis);
        }
    }

    /**
     * Verifies that filter combined with search works correctly.
     */
    @Test
    public void filterByPeriod_combinedWithSearch_appliesBothFilters() {
        List<HistorySessionWithExercises> sessions = createTestSessions();
        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        // Apply filter first
        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);
        // Then search
        viewModel.searchHistory("Today");

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);

        // Should only return sessions matching "Today"
        for (HistorySessionWithExercises session : result) {
            assertTrue("Session name should contain 'Today'",
                session.session.getName().toLowerCase().contains("today"));
        }
    }

    /**
     * Verifies that switching filters updates the list correctly.
     */
    @Test
    public void filterByPeriod_switchingFilters_updatesListCorrectly() {
        List<HistorySessionWithExercises> sessions = createTestSessions();
        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        // Start with ALL
        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);
        int allCount = viewModel.getHistorySessions().getValue().size();

        // Switch to THIS_WEEK
        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_WEEK);
        int weekCount = viewModel.getHistorySessions().getValue().size();

        // Switch back to ALL
        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);
        int allCountAgain = viewModel.getHistorySessions().getValue().size();

        assertEquals("ALL should return same count", allCount, allCountAgain);
        assertTrue("THIS_WEEK should return less or equal sessions", weekCount <= allCount);
    }

    /**
     * Verifies that empty list is handled correctly with filters.
     */
    @Test
    public void filterByPeriod_withEmptyList_returnsEmptyList() {
        historyListLiveData.setValue(new Result.HistorySuccess(new ArrayList<>()));

        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_WEEK);

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Verifies that old sessions are filtered out by THIS_WEEK.
     */
    @Test
    public void filterByPeriod_THIS_WEEK_excludesOldSessions() {
        List<HistorySessionWithExercises> sessions = new ArrayList<>();

        // Add session from 2 weeks ago
        HistorySessionWithExercises oldSession = new HistorySessionWithExercises();
        oldSession.session = new HistorySession("Old Workout",
            System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000), 0);
        oldSession.exercises = new ArrayList<>();
        sessions.add(oldSession);

        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_WEEK);

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertTrue("Old session should be filtered out", result.isEmpty());
    }

    /**
     * Verifies that old sessions are filtered out by THIS_MONTH.
     */
    @Test
    public void filterByPeriod_THIS_MONTH_excludesOldSessions() {
        List<HistorySessionWithExercises> sessions = new ArrayList<>();

        // Add session from 2 months ago
        HistorySessionWithExercises oldSession = new HistorySessionWithExercises();
        oldSession.session = new HistorySession("Old Workout",
            System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000), 0);
        oldSession.exercises = new ArrayList<>();
        sessions.add(oldSession);

        historyListLiveData.setValue(new Result.HistorySuccess(sessions));

        viewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_MONTH);

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertTrue("Old session should be filtered out", result.isEmpty());
    }

    /**
     * Creates test sessions with different dates for testing filters.
     */
    private List<HistorySessionWithExercises> createTestSessions() {
        List<HistorySessionWithExercises> sessions = new ArrayList<>();

        // Session 1: Today
        HistorySessionWithExercises todaySession = new HistorySessionWithExercises();
        todaySession.session = new HistorySession("Today Workout", System.currentTimeMillis(), 0);
        todaySession.exercises = new ArrayList<>();
        sessions.add(todaySession);

        // Session 2: 10 days ago
        HistorySessionWithExercises lastWeekSession = new HistorySessionWithExercises();
        lastWeekSession.session = new HistorySession("Last Week Workout",
            System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000), 0);
        lastWeekSession.exercises = new ArrayList<>();
        sessions.add(lastWeekSession);

        // Session 3: 45 days ago
        HistorySessionWithExercises oldSession = new HistorySessionWithExercises();
        oldSession.session = new HistorySession("Old Workout",
            System.currentTimeMillis() - (45L * 24 * 60 * 60 * 1000), 0);
        oldSession.exercises = new ArrayList<>();
        sessions.add(oldSession);

        return sessions;
    }
}

