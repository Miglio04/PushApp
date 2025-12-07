package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pushapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    // --- ELEMENTI GRAFICI (UI) ---
    private GridLayout calendarGrid;
    private TextView txtMonthTitle;
    private ImageView btnExpand, btnPrev, btnNext;
    private LinearLayout legendLayout;

    // Grafici e Spinner
    private LineChart chartLoad; // Grafico Carichi
    private LineChart chartReps; // Grafico Ripetizioni
    private Spinner exerciseSpinner; // Menu a tendina

    // KPI (Numeri in alto)
    private TextView txtKpiWorkouts, txtKpiTime, txtKpiVolume;

    // --- LOGICA E STATO ---
    private LocalDate selectedDate = LocalDate.now();
    private boolean isMonthView = false;
    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private int calculatedItemWidth = 0;

    public StatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. COLLEGAMENTO VISTE (FIND VIEW BY ID)
        calendarGrid = view.findViewById(R.id.calendarGrid);
        txtMonthTitle = view.findViewById(R.id.txtMonthTitle);
        btnExpand = view.findViewById(R.id.btnExpand);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        legendLayout = view.findViewById(R.id.legendLayout);

        // Grafici e Spinner
        chartLoad = view.findViewById(R.id.chartLoad);
        chartReps = view.findViewById(R.id.chartReps);
        exerciseSpinner = view.findViewById(R.id.exerciseSpinner);

        // KPI
        txtKpiWorkouts = view.findViewById(R.id.txtKpiWorkouts);
        txtKpiTime = view.findViewById(R.id.txtKpiTime);
        txtKpiVolume = view.findViewById(R.id.txtKpiVolume);

        // 2. CONFIGURAZIONE INIZIALE
        calculateCellWidth();
        setupLegend();

        // Configura lo stile dei due grafici (vuoti all'inizio)
        setupChartStyle(chartLoad);
        setupChartStyle(chartReps);

        // Configura il menu a tendina e i dati dei grafici
        setupExerciseSpinner();

        // Disegna Calendario e Dati Mensili
        drawCalendar();
        updateMonthlyStats();

        // 3. EVENTI CLICK (LISTENERS)
        btnExpand.setOnClickListener(v -> {
            isMonthView = !isMonthView;
            btnExpand.animate().rotation(isMonthView ? 180f : 0f).start();
            drawCalendar();
        });

        btnPrev.setOnClickListener(v -> {
            if (isMonthView) selectedDate = selectedDate.minusMonths(1).withDayOfMonth(1);
            else selectedDate = selectedDate.minusWeeks(1);
            drawCalendar();
            updateMonthlyStats();
        });

        btnNext.setOnClickListener(v -> {
            if (isMonthView) selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1);
            else selectedDate = selectedDate.plusWeeks(1);
            drawCalendar();
            updateMonthlyStats();
        });
    }

    // --- CONFIGURAZIONE GRAFICI ---
    private void setupChartStyle(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // Asse X (Sotto)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.GRAY);

        // Assi Y
        chart.getAxisLeft().setTextColor(Color.GRAY);
        chart.getAxisRight().setEnabled(false); // Nasconde asse destro
    }

    // --- CONFIGURAZIONE SPINNER + DATI ---
    private void setupExerciseSpinner() {
        String[] exercises = {"Bench Press", "Squat", "Deadlift", "Overhead Press", "Pull Up"};

        // Usa il layout personalizzato per lo Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_custom,
                exercises
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_custom);
        exerciseSpinner.setAdapter(adapter);

        // Quando selezioni un esercizio, aggiorna i grafici
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = exercises[position];
                updateChartsData(selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateChartsData(String exerciseName) {
        List<Entry> loadEntries = new ArrayList<>();
        List<Entry> repsEntries = new ArrayList<>();

        // SIMULAZIONE DATI (Mock)
        if (exerciseName.equals("Bench Press")) {
            loadEntries.add(new Entry(1, 60f)); loadEntries.add(new Entry(2, 65f)); loadEntries.add(new Entry(3, 70f));
            repsEntries.add(new Entry(1, 10f)); repsEntries.add(new Entry(2, 10f)); repsEntries.add(new Entry(3, 8f));
        } else if (exerciseName.equals("Squat")) {
            loadEntries.add(new Entry(1, 80f)); loadEntries.add(new Entry(2, 90f)); loadEntries.add(new Entry(3, 100f));
            repsEntries.add(new Entry(1, 5f)); repsEntries.add(new Entry(2, 5f)); repsEntries.add(new Entry(3, 5f));
        } else {
            // Dati generici per altri esercizi
            loadEntries.add(new Entry(1, 40f)); loadEntries.add(new Entry(2, 42f)); loadEntries.add(new Entry(3, 45f));
            repsEntries.add(new Entry(1, 12f)); repsEntries.add(new Entry(2, 12f)); repsEntries.add(new Entry(3, 15f));
        }

        // Configura dataset LOAD (Blu)
        LineDataSet loadSet = new LineDataSet(loadEntries, "Load");
        loadSet.setColor(Color.parseColor("#005BB1"));
        loadSet.setCircleColor(Color.parseColor("#005BB1"));
        loadSet.setLineWidth(3f);
        loadSet.setCircleRadius(5f);
        loadSet.setDrawValues(false);
        loadSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Configura dataset REPS (Arancione)
        LineDataSet repsSet = new LineDataSet(repsEntries, "Reps");
        repsSet.setColor(Color.parseColor("#E65100"));
        repsSet.setCircleColor(Color.parseColor("#E65100"));
        repsSet.setLineWidth(3f);
        repsSet.setCircleRadius(5f);
        repsSet.setDrawValues(false);
        repsSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Aggiorna Grafici
        chartLoad.setData(new LineData(loadSet));
        chartLoad.animateX(800);

        chartReps.setData(new LineData(repsSet));
        chartReps.animateX(800);
    }

    // --- CALENDARIO E KPI ---
    private void updateMonthlyStats() {
        Month currentMonth = selectedDate.getMonth();
        int currentYear = selectedDate.getYear();

        if (currentMonth == Month.DECEMBER && currentYear == 2025) {
            txtKpiWorkouts.setText("3"); txtKpiTime.setText("04:30h"); txtKpiVolume.setText("14.5k");
        } else if (currentMonth == Month.NOVEMBER && currentYear == 2025) {
            txtKpiWorkouts.setText("12"); txtKpiTime.setText("18:00h"); txtKpiVolume.setText("48.2k");
        } else {
            txtKpiWorkouts.setText("0"); txtKpiTime.setText("00:00h"); txtKpiVolume.setText("0");
        }
    }

    private void calculateCellWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int totalMarginsPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
        calculatedItemWidth = (screenWidth - totalMarginsPx) / 7;
    }

    private void setupLegend() {
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        legendLayout.removeAllViews();
        legendLayout.setWeightSum(0);
        for (String day : days) {
            TextView tv = new TextView(getContext());
            tv.setText(day);
            tv.setTextColor(Color.WHITE); // Testo Bianco
            tv.setTextSize(12);
            tv.setGravity(Gravity.CENTER);
            legendLayout.addView(tv, new LinearLayout.LayoutParams(calculatedItemWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void drawCalendar() {
        calendarGrid.removeAllViews();
        txtMonthTitle.setText(selectedDate.format(monthFormatter).toUpperCase());

        List<LocalDate> daysToShow = new ArrayList<>();
        if (isMonthView) {
            YearMonth yearMonth = YearMonth.from(selectedDate);
            int daysInMonth = yearMonth.lengthOfMonth();
            LocalDate firstOfMonth = yearMonth.atDay(1);
            int emptyCells = firstOfMonth.getDayOfWeek().getValue() - 1;
            for(int i=0; i<emptyCells; i++) daysToShow.add(null);
            for(int i=1; i<=daysInMonth; i++) daysToShow.add(yearMonth.atDay(i));
        } else {
            LocalDate startOfWeek = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
            for(int i=0; i<7; i++) daysToShow.add(startOfWeek.plusDays(i));
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        LocalDate today = LocalDate.now();

        for (LocalDate date : daysToShow) {
            View view = inflater.inflate(R.layout.item_day_header, calendarGrid, false);
            TextView txtNum = view.findViewById(R.id.txtDayNumber);
            TextView txtName = view.findViewById(R.id.txtDayName);
            ImageView dot = view.findViewById(R.id.indicatorDot);

            if (txtName != null) txtName.setVisibility(View.GONE);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = calculatedItemWidth;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (date == null) {
                view.setVisibility(View.INVISIBLE);
            } else {
                txtNum.setText(String.valueOf(date.getDayOfMonth()));

                // COLORI CERCHI
                if (date.isEqual(today)) {
                    txtNum.setBackgroundResource(R.drawable.bg_circle_selection); // Pieno
                    txtNum.setTextColor(Color.parseColor("#005BB1"));
                } else if (date.isEqual(selectedDate)) {
                    txtNum.setBackgroundResource(R.drawable.bg_circle_outline); // Vuoto
                    txtNum.setTextColor(Color.WHITE);
                } else {
                    txtNum.setBackground(null);
                    txtNum.setTextColor(Color.WHITE);
                }

                // PALLINI
                if (isWorkoutDay(date)) {
                    dot.setVisibility(View.VISIBLE);
                    // Contrasto Pallino
                    if (date.isEqual(today)) dot.setColorFilter(Color.parseColor("#005BB1"));
                    else dot.setColorFilter(Color.WHITE);
                } else {
                    dot.setVisibility(View.INVISIBLE);
                }

                view.setOnClickListener(v -> {
                    selectedDate = date;
                    drawCalendar();
                    updateMonthlyStats();
                });
            }
            calendarGrid.addView(view, params);
        }
    }

    private boolean isWorkoutDay(LocalDate date) {
        int year = date.getYear();
        Month month = date.getMonth();
        int day = date.getDayOfMonth();
        if (year == 2025) {
            if (month == Month.DECEMBER) return day == 1 || day == 3 || day == 4;
            else if (month == Month.NOVEMBER) {
                DayOfWeek dow = date.getDayOfWeek();
                return dow == DayOfWeek.MONDAY || dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.FRIDAY;
            }
        }
        return false;
    }
}