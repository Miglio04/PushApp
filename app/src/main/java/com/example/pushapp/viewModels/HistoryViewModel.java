package com.example.pushapp.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.HistoryCallback;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.utils.KpiStats;
import com.example.pushapp.utils.WorkoutState;
import com.github.mikephil.charting.data.Entry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryViewModel extends ViewModel {
    private final HistoryRepository repository;
    private final MutableLiveData<List<HistorySessionWithExercises>> historySessions = new MutableLiveData<>();
    private List<HistorySessionWithExercises> fullHistoryList = new ArrayList<>();
    private String currentSearchQuery = "";
    private final MutableLiveData<KpiStats> kpiStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> selectedDateLiveData = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<Boolean> isMonthView = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> exerciseNamesLiveData = new MutableLiveData<>();
    private final MutableLiveData<GraphChartData> graphMaxWeightData = new MutableLiveData<>();
    private final MutableLiveData<GraphChartData> graphTotalVolumeData = new MutableLiveData<>();
    public static class GraphChartData {
        public final List<Entry> entries;
        public final List<GraphPoint> points;
        GraphChartData(List<Entry> entries, List<GraphPoint> points) {
            this.entries = entries;
            this.points = points;
        }
    }
    public enum ChartMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME
    }

    public HistoryViewModel(HistoryRepository repository) {
        this.repository = repository;
        this.repository.getHistoryList().observeForever(historyListObserver);
        fetchHistory();
    }

    public LiveData<List<HistorySessionWithExercises>> getHistorySessions() { return historySessions; }
    public LiveData<KpiStats> getKpiStats() {
        return kpiStatsLiveData;
    }
    public LiveData<LocalDate> getSelectedDate() {
        return selectedDateLiveData;
    }
    public LiveData<Boolean> isMonthView() {
        return isMonthView;
    }
    public LiveData<List<String>> getExerciseNames() {
        return exerciseNamesLiveData;
    }
    public LiveData<GraphChartData> getGraphMaxWeightData() {
        return graphMaxWeightData;
    }
    public LiveData<GraphChartData> getGraphTotalVolumeData() {
        return graphTotalVolumeData;
    }

    public void fetchHistory() {
        repository.fetchHistoryData();
    }

    public void searchHistory(String query) {
        this.currentSearchQuery = (query != null) ? query : "";
        if (fullHistoryList == null) {
            return;
        }

        if (currentSearchQuery.trim().isEmpty()) {
            historySessions.postValue(fullHistoryList);
            return;
        }

        List<HistorySessionWithExercises> filteredList = new ArrayList<>();
        String lowerCaseQuery = currentSearchQuery.toLowerCase(Locale.ROOT);

        for (HistorySessionWithExercises session : fullHistoryList) {
            if (session.session.getName().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) {
                filteredList.add(session);
                continue;
            }
            for (HistoryWorkoutExerciseWithSeries exercise : session.exercises) {
                if (exercise.historyWorkoutExercise.getExerciseName().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) {
                    filteredList.add(session);
                    break;
                }
            }
        }

        historySessions.postValue(filteredList);
    }

    public void saveWorkoutSession(HistorySessionWithExercises sessionToSave, Runnable onComplete) {
        if (sessionToSave == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        if (sessionToSave.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries ex : sessionToSave.exercises) {
                if (ex.historySeries != null) {
                    ex.historySeries.removeIf(serie -> serie.getReps() == 0);
                }
            }
            sessionToSave.exercises.removeIf(ex -> ex.historySeries == null || ex.historySeries.isEmpty());
        }

        if (sessionToSave.exercises == null || sessionToSave.exercises.isEmpty()) {
            Log.i("HistoryViewModel", "Workout session is empty after cleanup. Aborting save.");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        repository.saveWorkoutSession(sessionToSave, onComplete);
    }

    public WorkoutState createNewWorkoutSessionWithTemplate(Routine day) {
        HistorySessionWithExercises session = repository.createNewWorkoutSessionWithoutTemplate(day);
        if (session == null) return null;
        return new WorkoutState(session, day);
    }

    public void deleteSession(HistorySessionWithExercises wrapper) {
        if (wrapper != null && wrapper.session != null) {
            repository.deleteSession(wrapper.session.getHistorySessionId());
        }
    }

    public void next() {
        LocalDate current = selectedDateLiveData.getValue();
        if (current != null) {
            boolean isMonth = Boolean.TRUE.equals(isMonthView.getValue());
            selectedDateLiveData.setValue(isMonth ? current.plusMonths(1) : current.plusWeeks(1));
            recalculateKpisAndCalendar();
        }
    }

    public void previous() {
        LocalDate current = selectedDateLiveData.getValue();
        if (current != null) {
            boolean isMonth = Boolean.TRUE.equals(isMonthView.getValue());
            selectedDateLiveData.setValue(isMonth ? current.minusMonths(1) : current.minusWeeks(1));
            recalculateKpisAndCalendar();
        }
    }

    public void toggleCalendarView() {
        isMonthView.setValue(!Boolean.TRUE.equals(isMonthView.getValue()));
    }

    public void changeSelectedDate(LocalDate newDate) {
        selectedDateLiveData.setValue(newDate);
        recalculateKpisAndCalendar();
    }

    public void fetchGraphDataForExercise(String exerciseName, ChartMetric metric) {
        HistoryRepository.StatMetric repoMetric = (metric == ChartMetric.MAX_WEIGHT) ?
                HistoryRepository.StatMetric.MAX_WEIGHT : HistoryRepository.StatMetric.TOTAL_VOLUME;

        repository.fetchGraphData(exerciseName, repoMetric, new HistoryCallback() {
            @Override
            public void onSuccessGraphDataFromLocal(List<GraphPoint> points) {
                GraphChartData chartData = createChartDataFromPoints(points);
                if (metric == ChartMetric.TOTAL_VOLUME) {
                    graphTotalVolumeData.postValue(chartData);
                } else {
                    graphMaxWeightData.postValue(chartData);
                }
            }

            @Override
            public void onFailureFromLocal(Exception e) {
                if (metric == ChartMetric.TOTAL_VOLUME) {
                    graphTotalVolumeData.postValue(null);
                } else {
                    graphMaxWeightData.postValue(null);
                }
            }
        });
    }

    private final Observer<Result> historyListObserver = result -> {
        if (result instanceof Result.HistorySuccess) {
            List<HistorySessionWithExercises> sessions = ((Result.HistorySuccess) result).getData();
            onHistoryDataChanged(sessions);
            searchHistory(currentSearchQuery);
        }
    };

    private void onHistoryDataChanged(List<HistorySessionWithExercises> history) {
        this.fullHistoryList = (history != null) ? history : new ArrayList<>();
        extractExerciseNames(this.fullHistoryList);
        recalculateKpisAndCalendar();
    }

    private void recalculateKpisAndCalendar() {
        LocalDate currentDate = selectedDateLiveData.getValue();
        if (currentDate == null) return;
        calculateKpis(fullHistoryList, currentDate);
    }

    public void calculateKpis(List<HistorySessionWithExercises> history, LocalDate selectedDate) {
        if (history == null || history.isEmpty()) {
            kpiStatsLiveData.postValue(new KpiStats(0, 0.0, 0L, 0));
            return;
        }
        int workoutsMonth = 0;
        double volumeMonth = 0;
        long timeMillisMonth = 0;
        Set<LocalDate> allDates = new HashSet<>();
        int selMonth = selectedDate.getMonthValue();
        int selYear = selectedDate.getYear();
        for (HistorySessionWithExercises s : history) {
            LocalDate d = Instant.ofEpochMilli(s.session.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            allDates.add(d);
            if (d.getMonthValue() == selMonth && d.getYear() == selYear) {
                workoutsMonth++;
                timeMillisMonth += s.session.getDuration();
                for (var ex : s.exercises) {
                    for (HistorySerie sr : ex.historySeries) {
                        volumeMonth += (sr.getWeight() * sr.getReps());
                    }
                }
            }
        }
        int streak = 0;
        LocalDate check = LocalDate.now();
        if (!allDates.contains(check)) {
            check = check.minusDays(1);
        }
        while (allDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        kpiStatsLiveData.postValue(new KpiStats(workoutsMonth, volumeMonth, timeMillisMonth, streak));
    }

    public void extractExerciseNames(List<HistorySessionWithExercises> history) {
        if (history == null) {
            exerciseNamesLiveData.postValue(new ArrayList<>());
            return;
        }
        Set<String> nameSet = new HashSet<>();
        for (HistorySessionWithExercises s : history) {
            if (s.exercises != null) {
                for (var ex : s.exercises) {
                    if (ex.historyWorkoutExercise != null && ex.historyWorkoutExercise.getExerciseName() != null) {
                        nameSet.add(ex.historyWorkoutExercise.getExerciseName());
                    }
                }
            }
        }
        List<String> names = new ArrayList<>(nameSet);
        Collections.sort(names);
        exerciseNamesLiveData.postValue(names);
    }

    private GraphChartData createChartDataFromPoints(List<GraphPoint> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            entries.add(new Entry(i, points.get(i).getValue()));
        }
        return new GraphChartData(entries, points);
    }

    public List<LocalDate> getCalendarDays() {
        LocalDate date = selectedDateLiveData.getValue();
        if (date == null) return new ArrayList<>();
        List<LocalDate> days = new ArrayList<>();
        boolean isMonth = Boolean.TRUE.equals(isMonthView.getValue());
        if (isMonth) {
            YearMonth ym = YearMonth.from(date);
            int emptyCells = ym.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < emptyCells; i++) days.add(null);
            for (int i = 1; i <= ym.lengthOfMonth(); i++) days.add(ym.atDay(i));
        } else {
            LocalDate start = date.minusDays(date.getDayOfWeek().getValue() - 1);
            for (int i = 0; i < 7; i++) days.add(start.plusDays(i));
        }
        return days;
    }

    public boolean isWorkoutDay(LocalDate date) {
        if (date == null || fullHistoryList == null) return false;
        for (HistorySessionWithExercises s : fullHistoryList) {
            LocalDate workoutDate = Instant.ofEpochMilli(s.session.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            if (workoutDate.isEqual(date)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.getHistoryList().removeObserver(historyListObserver);
    }
}
