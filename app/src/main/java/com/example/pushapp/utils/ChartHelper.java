package com.example.pushapp.utils;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import com.example.pushapp.R;
import com.example.pushapp.models.GraphPoint;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartHelper {

    public static void setupChartStyle(LineChart chart, Context context) {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        int axisGridColor = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant);
        int axisLineColor = ContextCompat.getColor(context, R.color.md_theme_outlineVariant);
        int axisTextColor = ContextCompat.getColor(context, R.color.md_theme_onSurface);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGridColor(axisGridColor);
        leftAxis.setAxisLineColor(axisLineColor);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextColor(axisTextColor);
        leftAxis.setTextSize(11f);

        xAxis.setTextColor(axisTextColor);
        xAxis.setGridColor(axisGridColor);
        xAxis.setTextSize(11f);
    }

    public static void bindChart(LineChart chart, List<Entry> entries, String label, int color, List<GraphPoint> points, Context context) {
        if (entries.isEmpty()) {
            chart.clear();
            return;
        }

        LineDataSet set = createLineDataSet(entries, label, color, context);
        LineData ld = new LineData(set);
        chart.setData(ld);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGranularity(1f);

        configureXAxis(chart, points);

        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.getLegend().setEnabled(false);
        chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant));
        chart.animateX(800);
        chart.invalidate();
    }

    private static LineDataSet createLineDataSet(List<Entry> entries, String label, int color, Context context) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setColor(color);
        set.setLineWidth(3f);
        set.setCircleColor(color);
        set.setDrawFilled(true);
        set.setFillColor(color);
        set.setFillAlpha(140);
        set.setDrawValues(false);
        set.setCircleRadius(6f);
        set.setDrawCircles(true);
        set.setCircleHoleRadius(3f);
        set.setCircleHoleColor(ContextCompat.getColor(context, R.color.md_theme_background));
        set.setHighLightColor(ContextCompat.getColor(context, R.color.md_theme_secondary));
        set.setDrawHorizontalHighlightIndicator(false);
        return set;
    }

    private static void configureXAxis(LineChart chart, List<GraphPoint> points) {
        if (points == null || points.isEmpty()) return;

        try {
            List<String> labels = new ArrayList<>();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault());
            for (GraphPoint p : points) {
                String formatted = java.time.Instant.ofEpochMilli(p.getDate()).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(df);
                labels.add(formatted);
            }

            XAxis x = chart.getXAxis();
            x.setValueFormatter(new IndexAxisValueFormatter(labels));
            x.setLabelRotationAngle(-20f);
            x.setGranularity(1f);
            x.setGranularityEnabled(true);
            x.setLabelCount(Math.min(labels.size(), 6), true);
            x.setDrawAxisLine(true);
            x.setDrawLabels(true);
            x.setAvoidFirstLastClipping(true);
            x.setAxisMinimum(0f);
            x.setAxisMaximum(Math.max(0, labels.size() - 1));

            int maxVisible = Math.min(labels.size(), 6);
            chart.setVisibleXRangeMaximum(maxVisible);
            chart.moveViewToX(Math.max(0, labels.size() - maxVisible));
        } catch (Exception ignored) {}
    }
}
