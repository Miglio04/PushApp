package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.utils.KpiStats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryViewModel extends ViewModel {
    private final HistoryRepository repository;
    private final MutableLiveData<KpiStats> kpiStatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> exerciseNamesLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> selectedDateLiveData = new MutableLiveData<>(LocalDate.now());
    private List<HistorySessionWithExercises> fullHistoryList = new ArrayList<>();
    public enum ChartMetric {
        MAX_WEIGHT,
        TOTAL_VOLUME,
        ESTIMATED_1RM
    }
    public HistoryViewModel(HistoryRepository repository) { this.repository = repository; }
    public LiveData<KpiStats> getKpiStats() {
        return kpiStatsLiveData;
    }
    public LiveData<Result> getHistoryList() {
        return repository.getHistoryList();
    }
    public LiveData<List<String>> getExerciseNames() {
        return exerciseNamesLiveData;
    }
    public LiveData<LocalDate> getSelectedDate() {
        return selectedDateLiveData;
    }

    public void onHistoryDataChanged(List<HistorySessionWithExercises> history) {
        this.fullHistoryList = history;
        recalculateKpisAndCalendar();
    }

    private void recalculateKpisAndCalendar() {
        LocalDate currentDate = selectedDateLiveData.getValue();
        if (currentDate == null || fullHistoryList.isEmpty()) return;
        calculateKpis(fullHistoryList, currentDate);
    }

    public void extractExerciseNames(List<HistorySessionWithExercises> history) {
        if (history == null) {
            exerciseNamesLiveData.postValue(new ArrayList<>());
            return;
        }
        Set<String> nameSet = new HashSet<>();
        for (HistorySessionWithExercises s : history) {
            if (s.exercises != null) {
                for (HistoryWorkoutExerciseWithSeries ex : s.exercises) {
                    if (ex.historyWorkoutExercise != null && ex.historyWorkoutExercise.getExerciseName() != null) {
                        nameSet.add(ex.historyWorkoutExercise.getExerciseName());
                    }
                }
            }
        }
        List<String> names = new ArrayList<>(nameSet);
        java.util.Collections.sort(names);
        exerciseNamesLiveData.postValue(names);
    }

    public void fetchHistory() {
        repository.fetchHistoryData();
    }
    public void searchHistory(String query) {
        repository.searchHistory(query);
    }
    public void deleteSession(HistorySessionWithExercises wrapper) {
        if (wrapper != null && wrapper.session != null) repository.deleteSession(wrapper.session.getHistorySessionId());
    }
    public LiveData<Result> getGraphData() {
        return repository.getGraphData();
    }
    public void fetchGraphDataForExercise(String exerciseName, ChartMetric metric) {
        HistoryRepository.StatMetric repoMetric;
        switch (metric) {
            case TOTAL_VOLUME:
                repoMetric = HistoryRepository.StatMetric.TOTAL_VOLUME;
                break;
            case ESTIMATED_1RM:
                repoMetric = HistoryRepository.StatMetric.ESTIMATED_1RM;
                break;
            case MAX_WEIGHT:
            default:
                repoMetric = HistoryRepository.StatMetric.MAX_WEIGHT;
                break;
        }
        repository.fetchGraphData(exerciseName, repoMetric);
    }

    public void calculateKpis(List<HistorySessionWithExercises> history, LocalDate selectedDate) {
        if (history == null) return;
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
        KpiStats stats = new KpiStats(workoutsMonth, volumeMonth, timeMillisMonth, streak);
        kpiStatsLiveData.postValue(stats);
    }

    public void next(boolean isMonthView) {
        LocalDate current = selectedDateLiveData.getValue();
        if (current != null) {
            selectedDateLiveData.setValue(isMonthView ? current.plusMonths(1) : current.plusWeeks(1));
            recalculateKpisAndCalendar();
        }
    }

    public void previous(boolean isMonthView) {
        LocalDate current = selectedDateLiveData.getValue();
        if (current != null) {
            selectedDateLiveData.setValue(isMonthView ? current.minusMonths(1) : current.minusWeeks(1));
            recalculateKpisAndCalendar();
        }
    }

    public void changeSelectedDate(LocalDate newDate) {
        selectedDateLiveData.setValue(newDate);
        recalculateKpisAndCalendar();
    }
}