package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.gridlayout.widget.GridLayout;

import com.example.pushapp.R;
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

public class StatsFragment extends Fragment {

    private GridLayout calendarGrid;
    private TextView txtMonthTitle, txtKpiWorkouts, txtKpiVolume, txtKpiTime, txtStreakCount, txtStreakMessage;
    private ImageButton btnPrev, btnNext, btnExpand;
    private LineChart chartLoad, chartReps;
    private AutoCompleteTextView exerciseSpinner;
    private LinearLayout legendLayout;

    private HistoryViewModel historyViewModel;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        // use helper for chart styling
        ChartHelper.setupChartStyle(chartLoad, requireContext());
        ChartHelper.setupChartStyle(chartReps, requireContext());
        historyViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(requireContext())).get(HistoryViewModel.class);
        setupObservers();
        setupClickListeners();
        historyViewModel.fetchHistory();
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
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
        legendLayout = view.findViewById(R.id.legendLayout);
        setupLegend();
    }

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
            txtKpiVolume.setText(String.format(Locale.ENGLISH, "%.1fK", stats.getVolumeMonth() / 1000));
            long mins = stats.getTimeMillisMonth() / 60000;
            txtKpiTime.setText(mins > 60 ? (mins / 60) + "h " + (mins % 60) + "m" : mins + "m");
            int streak = stats.getCurrentStreak();
            txtStreakCount.setText(String.format(Locale.ENGLISH, "%d %s", streak, (streak == 1 ? "DAY STREAK!" : "DAYS STREAK!")));
            txtStreakMessage.setText(streak > 0 ? "You're on fire! 🔥" : "Start your streak today!");
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
                // use secondary color for the second chart to differentiate
                ChartHelper.bindChart(chartReps, chartData.entries, "Total Volume", ContextCompat.getColor(requireContext(), R.color.md_theme_secondary), chartData.points, requireContext());
            } else {
                chartReps.clear();
            }
        });
    }

    private void setupClickListeners() {
        btnPrev.setOnClickListener(v -> historyViewModel.previous());
        btnNext.setOnClickListener(v -> historyViewModel.next());
        btnExpand.setOnClickListener(v -> historyViewModel.toggleCalendarView());
    }

    private void drawCalendar() {
        LocalDate selectedDate = historyViewModel.getSelectedDate().getValue();
        if (selectedDate == null || getContext() == null) return;

        calendarGrid.removeAllViews();
        txtMonthTitle.setText(selectedDate.format(monthFormatter).toUpperCase());

        List<LocalDate> days = historyViewModel.getCalendarDays();

        LocalDate today = LocalDate.now();
        for (LocalDate date : days) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.item_day_header, calendarGrid, false);
            TextView tv = v.findViewById(R.id.txtDayNumber);
            ImageView dot = v.findViewById(R.id.indicatorDot);

            if (date != null) {
                tv.setText(String.valueOf(date.getDayOfMonth()));
                if (date.isEqual(today)) {
                    tv.setBackgroundResource(R.drawable.bg_circle_selection);
                    tv.setTextColor(Color.WHITE);
                } else if (date.isEqual(selectedDate)) {
                    tv.setBackgroundResource(R.drawable.bg_circle_outline);
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
                } else {
                    tv.setBackground(null);
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface));
                }

                dot.setVisibility(historyViewModel.isWorkoutDay(date) ? View.VISIBLE : View.INVISIBLE);
                dot.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
                v.setOnClickListener(v1 -> historyViewModel.changeSelectedDate(date));
            } else {
                v.setVisibility(View.INVISIBLE);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            params.width = 0;
            calendarGrid.addView(v, params);
        }
    }

    private void setupExerciseSpinner(List<String> names) {
        if (getContext() == null || names == null) return;
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
                try {
                    exerciseSpinner.setText(first, false);
                    loadChartsForExercise(first);
                } catch (Exception e) { /* ignored */ }
            });
        }
    }

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

    private void setupLegend() {
        legendLayout.removeAllViews();
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (String d : days) {
            TextView tv = new TextView(getContext());
            tv.setText(d);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
            tv.setTypeface(null, Typeface.BOLD);
            legendLayout.addView(tv);
        }
    }
}
