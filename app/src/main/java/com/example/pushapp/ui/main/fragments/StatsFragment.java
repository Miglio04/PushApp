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
import com.example.pushapp.models.Result;
import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
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
    private List<HistorySessionWithExercises> fullHistory = new ArrayList<>();
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

        historyViewModel = new ViewModelProvider(this, new ViewModelFactory(requireContext())).get(HistoryViewModel.class);

        historyViewModel.getHistoryList().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.HistorySuccess) {
                this.fullHistory = ((Result.HistorySuccess) result).getData();
                setupExerciseSpinner();
                drawCalendar();
                updateStats();
            }
        });

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

    private void setupClickListeners() {
        btnPrev.setOnClickListener(v -> {
            selectedDate = isMonthView ? selectedDate.minusMonths(1) : selectedDate.minusWeeks(1);
            drawCalendar();
            updateStats();
        });

        btnNext.setOnClickListener(v -> {
            selectedDate = isMonthView ? selectedDate.plusMonths(1) : selectedDate.plusWeeks(1);
            drawCalendar();
            updateStats();
        });

        btnExpand.setOnClickListener(v -> {
            isMonthView = !isMonthView;
            // Ruota la freccia: 90 gradi su (aperto), 270 gradi giù (chiuso)
            btnExpand.animate().rotation(isMonthView ? 90f : 270f).setDuration(300).start();
            drawCalendar();
        });
    }

    private void drawCalendar() {
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

                dot.setVisibility(isWorkoutDay(date) ? View.VISIBLE : View.INVISIBLE);
                dot.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_primary));

                v.setOnClickListener(v1 -> {
                    selectedDate = date;
                    drawCalendar();
                    updateStats();
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

    private void updateStats() {
        int workoutsMonth = 0;
        double volumeMonth = 0;
        long timeMillisMonth = 0;
        Set<LocalDate> allDates = new HashSet<>();

        int selMonth = selectedDate.getMonthValue();
        int selYear = selectedDate.getYear();

        for (HistorySessionWithExercises s : fullHistory) {
            LocalDate d = Instant.ofEpochMilli(s.session.startTime).atZone(ZoneId.systemDefault()).toLocalDate();
            allDates.add(d);

            if (d.getMonthValue() == selMonth && d.getYear() == selYear) {
                workoutsMonth++;
                timeMillisMonth += (s.session.endTime - s.session.startTime);
                for (var ex : s.exercises) {
                    for (HistorySerie sr : ex.historySeries) volumeMonth += (sr.weight * sr.reps);
                }
            }
        }

        txtKpiWorkouts.setText(String.valueOf(workoutsMonth));
        txtKpiVolume.setText(String.format(Locale.ENGLISH, "%.1fK", volumeMonth / 1000));
        long mins = timeMillisMonth / 60000;
        txtKpiTime.setText(mins > 60 ? (mins/60)+"h "+(mins%60)+"m" : mins+"m");

        // --- LOGICA STREAK 🔥 ---
        int streak = 0;
        LocalDate check = LocalDate.now();
        if (!allDates.contains(check)) check = check.minusDays(1);
        while (allDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        txtStreakCount.setText(streak + (streak == 1 ? " DAY STREAK!" : " DAYS STREAK!"));
        txtStreakMessage.setText(streak > 0 ? "You're on fire! 🔥" : "Start your streak today!");
    }

    private void setupExerciseSpinner() {
        List<String> names = new ArrayList<>();
        for (var s : fullHistory) {
            for (var ex : s.exercises) {
                if (!names.contains(ex.historyWorkoutExercise.exerciseName))
                    names.add(ex.historyWorkoutExercise.exerciseName);
            }
        }
        Collections.sort(names);
        if (names.isEmpty()) names.add("No Data");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                // Rotazione estetica della freccia al click
                imgSpinnerArrow.animate().rotationBy(360f).setDuration(400).start();
                updateCharts(names.get(pos));
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void updateCharts(String exerciseName) {
        List<Entry> loadEntries = new ArrayList<>();
        List<Entry> repsEntries = new ArrayList<>();
        fullHistory.sort((a, b) -> Long.compare(a.session.startTime, b.session.startTime));

        int x = 0;
        for (var s : fullHistory) {
            for (var ex : s.exercises) {
                if (ex.historyWorkoutExercise.exerciseName.equalsIgnoreCase(exerciseName)) {
                    float maxW = 0; float totalR = 0;
                    for (var sr : ex.historySeries) {
                        if (sr.weight > maxW) maxW = (float) sr.weight;
                        totalR += sr.reps;
                    }
                    loadEntries.add(new Entry(x, maxW));
                    repsEntries.add(new Entry(x, totalR));
                    x++;
                }
            }
        }
        renderChart(chartLoad, loadEntries, "Max Load (kg)", ContextCompat.getColor(requireContext(), R.color.md_theme_primary));
        renderChart(chartReps, repsEntries, "Total Reps", Color.parseColor("#FF9800"));
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

    private boolean isWorkoutDay(LocalDate d) {
        for (var s : fullHistory) {
            if (Instant.ofEpochMilli(s.session.startTime).atZone(ZoneId.systemDefault()).toLocalDate().isEqual(d)) return true;
        }
        return false;
    }
}