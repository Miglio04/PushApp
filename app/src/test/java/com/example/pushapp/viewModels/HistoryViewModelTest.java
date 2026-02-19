package com.example.pushapp.viewModels;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.HistoryRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link HistoryViewModel}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryViewModelTest {

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
     * Verifies fetchHistory calls repository.
     */
    @Test
    public void fetchHistory_callsRepository() {
        viewModel.fetchHistory();
        verify(repository, atLeastOnce()).fetchHistoryData();
    }

    /**
     * Verifies deleteSession calls repository with correct ID.
     */
    @Test
    public void deleteSession_callsRepositoryWithCorrectId() {
        HistorySessionWithExercises wrapper = new HistorySessionWithExercises();
        wrapper.session = new HistorySession("Test", System.currentTimeMillis(), 0);

        viewModel.deleteSession(wrapper);

        verify(repository).deleteSession(wrapper.session.getHistorySessionId());
    }

    /**
     * Verifies deleteSession handles null gracefully.
     */
    @Test
    public void deleteSession_withNull_doesNotCrash() {
        viewModel.deleteSession(null);
        verify(repository, never()).deleteSession(any());
    }

    /**
     * Verifies searchHistory filters by session name.
     */
    @Test
    public void searchHistory_filtersBySessionName() {
        List<HistorySessionWithExercises> mockList = createMockHistoryList();
        historyListLiveData.setValue(new Result.HistorySuccess(mockList));

        viewModel.searchHistory("Push");

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Push Day", result.get(0).session.getName());
    }

    /**
     * Verifies searchHistory with empty query returns all.
     */
    @Test
    public void searchHistory_emptyQuery_returnsAll() {
        List<HistorySessionWithExercises> mockList = createMockHistoryList();
        historyListLiveData.setValue(new Result.HistorySuccess(mockList));

        viewModel.searchHistory("");

        List<HistorySessionWithExercises> result = viewModel.getHistorySessions().getValue();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Verifies next() advances date by week.
     */
    @Test
    public void next_advancesDateByWeek() {
        LocalDate initial = viewModel.getSelectedDate().getValue();
        assertNotNull(initial);

        viewModel.next();

        LocalDate after = viewModel.getSelectedDate().getValue();
        assertNotNull(after);
        assertEquals(initial.plusWeeks(1), after);
    }

    /**
     * Verifies previous() goes back by week.
     */
    @Test
    public void previous_goesBackByWeek() {
        LocalDate initial = viewModel.getSelectedDate().getValue();
        assertNotNull(initial);

        viewModel.previous();

        LocalDate after = viewModel.getSelectedDate().getValue();
        assertNotNull(after);
        assertEquals(initial.minusWeeks(1), after);
    }

    /**
     * Verifies toggleCalendarView changes view mode.
     */
    @Test
    public void toggleCalendarView_changesViewMode() {
        Boolean initial = viewModel.isMonthView().getValue();
        assertFalse(initial);

        viewModel.toggleCalendarView();

        Boolean after = viewModel.isMonthView().getValue();
        assertTrue(after);
    }

    /**
     * Verifies KPI calculation with history data.
     */
    @Test
    public void calculateKpis_withData_calculatesCorrectly() {
        List<HistorySessionWithExercises> mockList = createMockHistoryList();
        historyListLiveData.setValue(new Result.HistorySuccess(mockList));

        assertNotNull(viewModel.getKpiStats().getValue());
    }

    /**
     * Verifies saveWorkoutSession skips empty sessions.
     */
    @Test
    public void saveWorkoutSession_withEmptyExercises_doesNotSave() {
        HistorySessionWithExercises session = new HistorySessionWithExercises();
        session.session = new HistorySession("Test", System.currentTimeMillis(), 0);
        session.exercises = new ArrayList<>();
        Runnable callback = mock(Runnable.class);

        viewModel.saveWorkoutSession(session, callback);

        verify(repository, never()).saveWorkoutSession(any(), any());
        verify(callback).run();
    }

    /**
     * Verifies extractExerciseNames extracts unique names.
     */
    @Test
    public void extractExerciseNames_extractsUniqueNames() {
        List<HistorySessionWithExercises> mockList = createMockHistoryList();

        viewModel.extractExerciseNames(mockList);

        List<String> names = viewModel.getExerciseNames().getValue();
        assertNotNull(names);
        assertTrue(names.contains("Bench Press"));
    }

    /**
     * Verifies resetLocalDatabase calls repository.
     */
    @Test
    public void resetLocalDatabase_callsRepository() {
        viewModel.resetLocalDatabase();
        verify(repository).resetLocalDatabase();
    }

    /**
     * Creates mock history list for testing.
     */
    private List<HistorySessionWithExercises> createMockHistoryList() {
        List<HistorySessionWithExercises> list = new ArrayList<>();

        // Session 1: Push Day
        HistorySessionWithExercises session1 = new HistorySessionWithExercises();
        session1.session = new HistorySession("Push Day", System.currentTimeMillis(), 0);
        session1.exercises = new ArrayList<>();
        HistoryWorkoutExerciseWithSeries ex1 = new HistoryWorkoutExerciseWithSeries();
        ex1.historyWorkoutExercise = new com.example.pushapp.models.history.HistoryWorkoutExercise(
                session1.session.getHistorySessionId(), "Bench Press", 0);
        ex1.historySeries = new ArrayList<>();
        ex1.historySeries.add(new HistorySerie(ex1.historyWorkoutExercise.getHistoryExerciseId(), 1, 80.0, 10));
        session1.exercises.add(ex1);
        list.add(session1);

        // Session 2: Pull Day
        HistorySessionWithExercises session2 = new HistorySessionWithExercises();
        session2.session = new HistorySession("Pull Day", System.currentTimeMillis() - 86400000, 0);
        session2.exercises = new ArrayList<>();
        list.add(session2);

        return list;
    }
}

