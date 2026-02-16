package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.utils.KpiStats;
import com.github.mikephil.charting.data.Entry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryViewModel extends ViewModel {

    public static class GraphChartData {
        public final List<Entry> entries;
        public final List<GraphPoint> points;

        GraphChartData(List<Entry> entries, List<GraphPoint> points) {
            this.entries = entries;
            this.points = points;
        }
    }

    private final HistoryRepository repository;
    private final MutableLiveData<KpiStats> kpiStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> exerciseNamesLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> selectedDateLiveData = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<Boolean> isMonthView = new MutableLiveData<>(false);
    private final MutableLiveData<List<HistorySessionWithExercises>> historySessions = new MutableLiveData<>();
    private List<HistorySessionWithExercises> fullHistoryList = new ArrayList<>();

    private final MutableLiveData<GraphChartData> graphMaxWeightData = new MutableLiveData<>();
    private final MutableLiveData<GraphChartData> graphTotalVolumeData = new MutableLiveData<>();

    private final Observer<Result> repoGraphObserver = result -> {
        if (result instanceof Result.GraphSuccess) {
            List<GraphPoint> points = ((Result.GraphSuccess) result).getData();
            if (points != null && !points.isEmpty()) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    entries.add(new Entry(i, points.get(i).getValue()));
                }
                graphMaxWeightData.postValue(new GraphChartData(entries, points));
            } else {
                graphMaxWeightData.postValue(null);
            }
        } else {
            graphMaxWeightData.postValue(null);
        }
    };

    private final Observer<Result> repoGraphVolumeObserver = result -> {
        if (result instanceof Result.GraphSuccess) {
            List<GraphPoint> points = ((Result.GraphSuccess) result).getData();
            if (points != null && !points.isEmpty()) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    entries.add(new Entry(i, points.get(i).getValue()));
                }
                graphTotalVolumeData.postValue(new GraphChartData(entries, points));
            } else {
                graphTotalVolumeData.postValue(null);
            }
        } else {
            graphTotalVolumeData.postValue(null);
        }
    };

    private final Observer<Result> historyListObserver = result -> {
        if (result instanceof Result.HistorySuccess) {
            List<HistorySessionWithExercises> sessions = ((Result.HistorySuccess) result).getData();
            historySessions.postValue(sessions);
            onHistoryDataChanged(sessions);
        }
    };

    public enum ChartMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME
    }

    public LiveData<GraphChartData> getGraphMaxWeightData() {
        return graphMaxWeightData;
    }

    public LiveData<GraphChartData> getGraphTotalVolumeData() {
        return graphTotalVolumeData;
    }

    public HistoryViewModel(HistoryRepository repository) {
        this.repository = repository;
        this.repository.getGraphData().observeForever(repoGraphObserver);
        this.repository.getGraphVolumeData().observeForever(repoGraphVolumeObserver);
        this.repository.getHistoryList().observeForever(historyListObserver);
    }

    public LiveData<KpiStats> getKpiStats() {
        return kpiStatsLiveData;
    }

    public LiveData<List<HistorySessionWithExercises>> getHistorySessions() {
        return historySessions;
    }

    public LiveData<List<String>> getExerciseNames() {
        return exerciseNamesLiveData;
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDateLiveData;
    }

    public LiveData<Boolean> isMonthView() {
        return isMonthView;
    }

    private void onHistoryDataChanged(List<HistorySessionWithExercises> history) {
        this.fullHistoryList = history;
        extractExerciseNames(history);
        recalculateKpisAndCalendar();
    }

    private void recalculateKpisAndCalendar() {
        LocalDate currentDate = selectedDateLiveData.getValue();
        if (currentDate == null) return;
        calculateKpis(fullHistoryList, currentDate);
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

    public void fetchHistory() {
        repository.fetchHistoryData();
    }

    public void searchHistory(String query) {
        repository.searchHistory(query);
    }

    public void deleteSession(HistorySessionWithExercises wrapper) {
        if (wrapper != null && wrapper.session != null) {
            repository.deleteSession(wrapper.session.getHistorySessionId());
        }
    }

    public void fetchGraphDataForExercise(String exerciseName, ChartMetric metric) {
        HistoryRepository.StatMetric repoMetric = (metric == ChartMetric.MAX_WEIGHT) ?
                HistoryRepository.StatMetric.MAX_WEIGHT : HistoryRepository.StatMetric.TOTAL_VOLUME;
        repository.fetchGraphData(exerciseName, repoMetric);
    }

    public void calculateKpis(List<HistorySessionWithExercises> history, LocalDate selectedDate) {
        if (history == null || history.isEmpty()) {
            kpiStatsLiveData.postValue(new KpiStats(0, 0, 0, 0));
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

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.getGraphData().removeObserver(repoGraphObserver);
        repository.getGraphVolumeData().removeObserver(repoGraphVolumeObserver);
        repository.getHistoryList().removeObserver(historyListObserver);
    }

    public void resetLocalDatabase(){
        repository.resetLocalDatabase();
    }
}
