package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.adapter.CalendarAdapter;
import com.example.pushapp.utils.ChartHelper;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for displaying user statistics.
 * Shows a calendar view of workout history, Key Performance Indicators (KPIs),
 * and line charts for exercise progress (Max Weight and Total Volume).
 */
public class StatsFragment extends Fragment {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView txtMonthTitle, txtKpiWorkouts, txtKpiVolume, txtKpiTime, txtStreakCount, txtStreakMessage;
    private ImageButton btnPrev, btnNext, btnExpand;
    private LineChart chartLoad, chartReps;
    private AutoCompleteTextView exerciseSpinner;
    private HistoryViewModel historyViewModel;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    /**
     * Initializes the HistoryViewModel.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(requireContext())).get(HistoryViewModel.class);
    }

    /**
     * Inflates the layout for the statistics screen.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    /**
     * Sets up views, styles, observers, and listeners after the view is created.
     *
     * @param view               The root view.
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        ChartHelper.setupChartStyle(chartLoad, requireContext());
        ChartHelper.setupChartStyle(chartReps, requireContext());
        setupObservers();
        setupClickListeners();
    }

    /**
     * Initializes UI references from the fragment layout.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        txtMonthTitle = view.findViewById(R.id.txtMonthTitle);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        btnExpand = view.findViewById(R.id.btnExpand);
        chartLoad = view.findViewById(R.id.chartLoad);
        chartReps = view.findViewById(R.id.chartReps);
        exerciseSpinner = view.findViewById(R.id.exerciseSpinner);
        txtKpiWorkouts = view.findViewById(R.id.txtKpiWorkouts);
        txtKpiVolume = view.findViewById(R.id.txtKpiVolume);
        txtKpiTime = view.findViewById(R.id.txtKpiTime);
        txtStreakCount = view.findViewById(R.id.txtStreakCount);
        txtStreakMessage = view.findViewById(R.id.txtStreakMessage);
        setupCalendar();
    }

    /**
     * Sets up observers for ViewModel LiveData to update the UI (calendar, KPIs, charts) whenever data changes.
     */
    private void setupObservers() {
        historyViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> drawCalendar());

        historyViewModel.isMonthView().observe(getViewLifecycleOwner(), isMonthView -> {
            btnExpand.animate().rotation(isMonthView ? 90f : 270f).setDuration(300).start();
            drawCalendar();
        });

        historyViewModel.getExerciseNames().observe(getViewLifecycleOwner(), this::setupExerciseSpinner);

        historyViewModel.getKpiStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            txtKpiWorkouts.setText(String.valueOf(stats.getWorkoutsMonth()));
            txtKpiVolume.setText(stats.getFormattedVolume());
            txtKpiTime.setText(stats.getFormattedTime());
            txtStreakCount.setText(stats.getFormattedStreakCountText());
            txtStreakMessage.setText(stats.getFormattedStreakMessageText());
        });

        historyViewModel.getGraphMaxWeightData().observe(getViewLifecycleOwner(), chartData -> {
            if (chartData != null) {
                ChartHelper.bindChart(chartLoad, chartData.entries, "Max Weight", ContextCompat.getColor(requireContext(), R.color.md_theme_primary), chartData.points, requireContext());
            } else {
                chartLoad.clear();
            }
        });

        historyViewModel.getGraphTotalVolumeData().observe(getViewLifecycleOwner(), chartData -> {
            if (chartData != null) {
                ChartHelper.bindChart(chartReps, chartData.entries, "Total Volume", ContextCompat.getColor(requireContext(), R.color.md_theme_secondary), chartData.points, requireContext());
            } else {
                chartReps.clear();
            }
        });
    }

    /**
     * Configures click listeners for calendar navigation and view toggling buttons.
     */
    private void setupClickListeners() {
        btnPrev.setOnClickListener(v -> historyViewModel.previous());
        btnNext.setOnClickListener(v -> historyViewModel.next());
        btnExpand.setOnClickListener(v -> historyViewModel.toggleCalendarView());
    }

    /**
     * Refreshes the calendar view based on the currently selected date.
     * Updates the month title and the list of days.
     */
    private void drawCalendar() {
        LocalDate selectedDate = historyViewModel.getSelectedDate().getValue();
        if (selectedDate == null) return;

        txtMonthTitle.setText(selectedDate.format(monthFormatter).toUpperCase());
        List<LocalDate> days = historyViewModel.getCalendarDays();
        calendarAdapter.updateDays(days, selectedDate);
    }

    /**
     * populates the exercise spinner with available exercise names for filtering charts.
     * Automatically selects the first exercise if available.
     *
     * @param names List of exercise names.
     */
    private void setupExerciseSpinner(List<String> names) {
        if (names == null) return;
        List<String> spinnerNames = new ArrayList<>(names);
        if (spinnerNames.isEmpty()) {
            spinnerNames.add("No Data");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, spinnerNames);
        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedExercise = spinnerNames.get(position);
            loadChartsForExercise(selectedExercise);
        });

        if (!spinnerNames.isEmpty() && !"No Data".equals(spinnerNames.get(0))) {
            String first = spinnerNames.get(0);
            exerciseSpinner.post(() -> {
                if (!isAdded()) return;
                try {
                    exerciseSpinner.setText(first, false);
                    loadChartsForExercise(first);
                } catch (Exception e) { /* ignored */ }
            });
        }
    }

    /**
     * Triggers data fetching for the charts based on the selected exercise.
     *
     * @param exerciseName The name of the selected exercise.
     */
    private void loadChartsForExercise(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty() || "No Data".equals(exerciseName)) {
            chartLoad.clear();
            chartReps.clear();
            if (getView() != null) {
                Snackbar.make(requireView(), "No data for selected exercise", Snackbar.LENGTH_SHORT).show();
            }
            return;
        }
        String trimmedExerciseName = exerciseName.trim();
        chartLoad.clear();
        chartReps.clear();
        historyViewModel.fetchGraphDataForExercise(trimmedExerciseName, HistoryViewModel.ChartMetric.MAX_WEIGHT);
        historyViewModel.fetchGraphDataForExercise(trimmedExerciseName, HistoryViewModel.ChartMetric.TOTAL_VOLUME);
    }

    /**
     * Configures the calendar RecyclerView and its adapter.
     */
    private void setupCalendar() {
        calendarAdapter = new CalendarAdapter(
                date -> historyViewModel.changeSelectedDate(date),
                date -> historyViewModel.isWorkoutDay(date)
        );

        calendarRecyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
    }
}
