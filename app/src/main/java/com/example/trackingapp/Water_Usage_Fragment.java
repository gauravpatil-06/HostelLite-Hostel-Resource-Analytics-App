package com.example.trackingapp;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Water_Usage_Fragment extends Fragment {

    private UsageViewModel usageViewModel;
    private TextView tvWaterUsage, tvWaterGoal, tvWaterPrediction, tvWaterAlerts;
    private PieChart pieChartWater;
    private BarChart barChartWeekly, barChartMonthly;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthYearSdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d (EEE)", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_water__usage_, container, false);
        usageViewModel = new ViewModelProvider(requireActivity()).get(UsageViewModel.class);
        initializeUI(view);
        observeViewModel();
        return view;
    }

    private void initializeUI(View view) {
        tvWaterUsage = view.findViewById(R.id.tvWaterUsage);
        tvWaterGoal = view.findViewById(R.id.tvWaterGoal);
        tvWaterPrediction = view.findViewById(R.id.tvWaterPrediction);
        tvWaterAlerts = view.findViewById(R.id.tvWaterAlerts);
        pieChartWater = view.findViewById(R.id.pieChartWater);
        barChartWeekly = view.findViewById(R.id.barChartWeekly);
        barChartMonthly = view.findViewById(R.id.barChartMonthly);
    }

    private void observeViewModel() {
        usageViewModel.idealWaterGoal.observe(getViewLifecycleOwner(), goal -> updateAllUI());
        usageViewModel.allWaterData.observe(getViewLifecycleOwner(), waterDataMap -> updateAllUI());
        usageViewModel.selectedDate.observe(getViewLifecycleOwner(), date -> updateAllUI());

        usageViewModel.weeklyWaterData.observe(getViewLifecycleOwner(), this::updateWeeklyBarChart);
        usageViewModel.monthlyAggregatedWaterData.observe(getViewLifecycleOwner(), this::updateMonthlyBarChart);
    }

    private void updateAllUI() {
        if (!isAdded() || usageViewModel == null) return;

        Map<String, Long> waterDataMap = usageViewModel.allWaterData.getValue();
        String currentDate = usageViewModel.selectedDate.getValue();
        if (waterDataMap == null || currentDate == null) return;

        Long usage = waterDataMap.getOrDefault(currentDate, 0L);
        Long goal = usageViewModel.idealWaterGoal.getValue();

        if (usage != null) {
            tvWaterUsage.setText(String.format(Locale.getDefault(), "Usage: %d L", usage));
        } else {
            tvWaterUsage.setText("Usage: 0 L");
        }

        if (goal != null && goal > 0) {
            tvWaterGoal.setText(String.format(Locale.getDefault(), "Ideal Goal: %d L", goal));
        } else {
            tvWaterGoal.setText("Ideal Goal: Not set");
        }

        updateWaterPieChart(usage, goal);
        updateInsights(usage, goal);
    }

    private void updateInsights(Long usage, Long goal) {
        Long minGoal = usageViewModel.minWaterGoal.getValue();
        Long maxGoal = usageViewModel.maxWaterGoal.getValue();

        if (usage == null || minGoal == null || goal == null || maxGoal == null || goal == 0) {
            tvWaterAlerts.setText("Alerts: Set a goal to see alerts.");
            tvWaterAlerts.setTextColor(getThemeTextColor());
            return;
        }

        if (usage > maxGoal) {
            tvWaterAlerts.setText("🚨 CRITICAL: Max limit exceeded!");
            tvWaterAlerts.setTextColor(Color.RED);
        } else if (usage > goal || usage < minGoal) {
            tvWaterAlerts.setText("⚠️ WARNING: Outside ideal range.");
            tvWaterAlerts.setTextColor(Color.parseColor("#FFA000"));
        } else {
            tvWaterAlerts.setText("✅ OPTIMAL: Usage is good.");
            tvWaterAlerts.setTextColor(Color.parseColor("#388E3C"));
        }
    }

    private void updateWaterPieChart(Long usage, Long goal) {
        if (usage == null || goal == null || goal == 0) {
            pieChartWater.clear();
            pieChartWater.invalidate();
            return;
        }

        float rawPercent = ((float) usage / goal * 100f);
        float chartPercent = Math.min(100f, rawPercent);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(chartPercent, "Used"));
        if (chartPercent < 100) {
            entries.add(new PieEntry(100f - chartPercent, "Remaining"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Daily Goal");
        dataSet.setColors(Color.parseColor("#00BCD4"), Color.parseColor("#B2EBF2"));
        dataSet.setValueTextColor(Color.TRANSPARENT);
        PieData pieData = new PieData(dataSet);

        pieChartWater.setUsePercentValues(true);
        pieChartWater.setData(pieData);
        pieChartWater.getDescription().setEnabled(false);
        pieChartWater.setHoleRadius(70f);
        pieChartWater.setCenterText(String.format(Locale.getDefault(), "%.0f%%\nUsed", rawPercent));
        pieChartWater.setCenterTextSize(18f);
        pieChartWater.setCenterTextColor(Color.parseColor("#00838F"));
        pieChartWater.getLegend().setEnabled(true);
        pieChartWater.animateY(1000);
        pieChartWater.invalidate();
    }

    private void updateWeeklyBarChart(Map<String, Long> usageMap) {
        updateBarChart(barChartWeekly, usageMap, dayFormat, "Weekly Water Usage", Color.parseColor("#00BCD4"), sdf);
    }

    private void updateMonthlyBarChart(Map<String, Long> usageMap) {
        updateBarChart(barChartMonthly, usageMap, monthYearFormat, "Monthly Water Usage", Color.parseColor("#009688"), monthYearSdf);
    }

    private void updateBarChart(BarChart chart, Map<String, Long> usageMap, SimpleDateFormat formatter, String label, int color, SimpleDateFormat parser) {
        if (usageMap == null || usageMap.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }
        ArrayList<String> labels = new ArrayList<>(usageMap.keySet());
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            entries.add(new BarEntry(i, usageMap.getOrDefault(labels.get(i), 0L)));
        }
        int textColor = getThemeTextColor();
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(textColor);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                return String.valueOf((int) value);
            }
        });
        barData.setValueTextSize(9f);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(textColor);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.stream().map(date -> formatDate(date, formatter, parser)).collect(Collectors.toList())));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextColor(textColor);
        chart.animateY(1200);
        chart.invalidate();
    }

    private String formatDate(String dateStr, SimpleDateFormat formatter, SimpleDateFormat parser) {
        try {
            Date date = parser.parse(dateStr);
            return formatter.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue();
        if (getContext() == null) return Color.BLACK;
        getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = getContext().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int color = arr.getColor(0, Color.BLACK);
        arr.recycle();
        return color;
    }
}