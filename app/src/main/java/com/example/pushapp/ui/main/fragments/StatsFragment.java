package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.gridlayout.widget.GridLayout;

import com.example.pushapp.R;
import com.example.pushapp.models.GraphPoint;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.example.pushapp.repositories.HistoryRepository;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StatsFragment extends Fragment {

    private GridLayout calendarGrid;
    private TextView txtMonthTitle, txtKpiWorkouts, txtKpiVolume, txtKpiTime, txtStreakCount, txtStreakMessage;
    private ImageButton btnPrev, btnNext, btnExpand;
    private ImageView imgSpinnerArrow;
    private LineChart chartLoad, chartReps;
    private Spinner exerciseSpinner;
    private LinearLayout legendLayout;

    private HistoryViewModel historyViewModel;
    private LocalDate selectedDate = LocalDate.now();
    private boolean isMonthView = false; // Default: vista settimanale
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupChartStyle(chartLoad);
        setupChartStyle(chartReps);
        historyViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(requireContext())).get(HistoryViewModel.class);        setupObservers();
        setupClickListeners();

        historyViewModel.fetchHistory();
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
        txtMonthTitle = view.findViewById(R.id.txtMonthTitle);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        btnExpand = view.findViewById(R.id.btnExpand);
        imgSpinnerArrow = view.findViewById(R.id.imgSpinnerArrow);
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
        historyViewModel.getHistoryList().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.HistorySuccess) {
                List<HistorySessionWithExercises> history = ((Result.HistorySuccess) result).getData();
                historyViewModel.onHistoryDataChanged(history);
                drawCalendar(history);
            }
        });

        historyViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            selectedDate = date;
            Result result = historyViewModel.getHistoryList().getValue();
            if (result instanceof Result.HistorySuccess) {
                drawCalendar(((Result.HistorySuccess) result).getData());
            }
        });

        historyViewModel.getExerciseNames().observe(getViewLifecycleOwner(), names -> {
            if (names != null) {
                setupExerciseSpinner(names);
            }
        });

        historyViewModel.getKpiStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                txtKpiWorkouts.setText(String.valueOf(stats.workoutsMonth));
                txtKpiVolume.setText(String.format(Locale.ENGLISH, "%.1fK", stats.volumeMonth / 1000));

                long mins = stats.timeMillisMonth / 60000;
                txtKpiTime.setText(mins > 60 ? (mins / 60) + "h " + (mins % 60) + "m" : mins + "m");

                int streak = stats.currentStreak;
                txtStreakCount.setText(streak + (streak == 1 ? " DAY STREAK!" : " DAYS STREAK!"));
                txtStreakMessage.setText(streak > 0 ? "You're on fire! 🔥" : "Start your streak today!");
            }
        });

        historyViewModel.getGraphVolumeData().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.GraphSuccess) {
                List<GraphPoint> points = ((Result.GraphSuccess) result).getData();
                if (points.isEmpty()) {
                    chartLoad.clear();
                    return;
                }
                List<Entry> chartEntries = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    chartEntries.add(new Entry(i, (float) points.get(i).getValue()));
                }
                renderChart(chartReps, chartEntries, "Total Volume", ContextCompat.getColor(requireContext(), R.color.md_theme_secondary));
            } else {
                chartReps.clear();
            }
        });
    }


    private void setupClickListeners() {
        btnPrev.setOnClickListener(v -> historyViewModel.previous(isMonthView));
        btnNext.setOnClickListener(v -> historyViewModel.next(isMonthView));

        btnExpand.setOnClickListener(v -> {
            isMonthView = !isMonthView;
            btnExpand.animate().rotation(isMonthView ? 90f : 270f).setDuration(300).start();
            Result result = historyViewModel.getHistoryList().getValue();
            if (result instanceof Result.HistorySuccess) {
                drawCalendar(((Result.HistorySuccess) result).getData());
            }
        });
    }

    private void drawCalendar(List<HistorySessionWithExercises> history) {
        calendarGrid.removeAllViews();
        txtMonthTitle.setText(selectedDate.format(monthFormatter).toUpperCase());

        List<LocalDate> days = new ArrayList<>();
        if (isMonthView) {
            YearMonth ym = YearMonth.from(selectedDate);
            int emptyCells = ym.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < emptyCells; i++) days.add(null);
            for (int i = 1; i <= ym.lengthOfMonth(); i++) days.add(ym.atDay(i));
        } else {
            LocalDate start = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
            for (int i = 0; i < 7; i++) days.add(start.plusDays(i));
        }

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

                dot.setVisibility(isWorkoutDay(date, history) ? View.VISIBLE : View.INVISIBLE);
                dot.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));

                v.setOnClickListener(v1 -> {
                    historyViewModel.changeSelectedDate(date);
                });
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
        if (getContext() == null) return;

        List<String> spinnerNames = new ArrayList<>(names);
        if (spinnerNames.isEmpty()) {
            spinnerNames.add("No Data");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String selectedExercise = spinnerNames.get(pos);

                if (selectedExercise.equals("No Data")) {
                    chartLoad.clear();
                    chartReps.clear();
                    return;
                }

                imgSpinnerArrow.animate().rotationBy(360f).setDuration(400).start();
                historyViewModel.fetchGraphDataForExercise(selectedExercise, HistoryViewModel.ChartMetric.MAX_WEIGHT);
                historyViewModel.fetchGraphDataForExercise(selectedExercise, HistoryViewModel.ChartMetric.TOTAL_VOLUME);
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
                chartLoad.clear();
                chartReps.clear();
            }
        });
    }

    private void renderChart(LineChart chart, List<Entry> entries, String label, int color) {
        if (entries.isEmpty()) { chart.clear(); return; }
        LineDataSet set = new LineDataSet(entries, label);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setColor(color);
        set.setLineWidth(3f);
        set.setCircleColor(color);
        set.setDrawFilled(true);
        set.setFillColor(color);
        set.setFillAlpha(40);
        set.setDrawValues(false);

        chart.setData(new LineData(set));
        chart.animateX(800);
        chart.invalidate();
    }

    private void setupChartStyle(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setDrawAxisLine(false);
    }

    private void setupLegend() {
        legendLayout.removeAllViews();
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (String d : days) {
            TextView tv = new TextView(getContext());
            tv.setText(d); tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
            tv.setTypeface(null, Typeface.BOLD);
            legendLayout.addView(tv);
        }
    }

    private boolean isWorkoutDay(LocalDate date, List<HistorySessionWithExercises> history) {
        if (date == null || history == null) return false;
        for (HistorySessionWithExercises s : history) {
            LocalDate workoutDate = Instant.ofEpochMilli(s.session.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            if (workoutDate.isEqual(date)) {
                return true;
            }
        }
        return false;
    }
}