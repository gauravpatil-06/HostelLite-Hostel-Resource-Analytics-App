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

public class Electricity_Usage_Fragment extends Fragment {

    private UsageViewModel usageViewModel;
    private TextView tvElectricityUsage, tvElectricityGoal, tvElectricityPrediction, tvElectricityAlerts;
    private PieChart pieChartElectricity;
    private BarChart barChartWeekly, barChartMonthly;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthYearSdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d (EEE)", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_electricity__usage_, container, false);
        usageViewModel = new ViewModelProvider(requireActivity()).get(UsageViewModel.class);
        initializeUI(view);
        observeViewModel();
        return view;
    }

    private void initializeUI(View view) {
        tvElectricityUsage = view.findViewById(R.id.tvElectricityUsage);
        tvElectricityGoal = view.findViewById(R.id.tvElectricityGoal);
        tvElectricityPrediction = view.findViewById(R.id.tvElectricityPrediction);
        tvElectricityAlerts = view.findViewById(R.id.tvElectricityAlerts);
        pieChartElectricity = view.findViewById(R.id.pieChartElectricity);
        barChartWeekly = view.findViewById(R.id.barChartWeekly);
        barChartMonthly = view.findViewById(R.id.barChartMonthly);
    }

    private void observeViewModel() {
        usageViewModel.idealElectricityGoal.observe(getViewLifecycleOwner(), goal -> updateAllUI());
        usageViewModel.allElectricityData.observe(getViewLifecycleOwner(), elecDataMap -> updateAllUI());
        usageViewModel.selectedDate.observe(getViewLifecycleOwner(), date -> updateAllUI());

        usageViewModel.weeklyElectricityData.observe(getViewLifecycleOwner(), this::updateWeeklyBarChart);
        usageViewModel.monthlyAggregatedElectricityData.observe(getViewLifecycleOwner(), this::updateMonthlyBarChart);
    }

    private void updateAllUI() {
        if (!isAdded() || usageViewModel == null) return;

        Map<String, Long> elecDataMap = usageViewModel.allElectricityData.getValue();
        String currentDate = usageViewModel.selectedDate.getValue();
        if (elecDataMap == null || currentDate == null) return;

        Long usage = elecDataMap.getOrDefault(currentDate, 0L);
        Long goal = usageViewModel.idealElectricityGoal.getValue();

        if (usage != null) {
            tvElectricityUsage.setText(String.format(Locale.getDefault(), "Usage: %d kWh", usage));
        } else {
            tvElectricityUsage.setText("Usage: 0 kWh");
        }

        if (goal != null && goal > 0) {
            tvElectricityGoal.setText(String.format(Locale.getDefault(), "Ideal Goal: %d kWh", goal));
        } else {
            tvElectricityGoal.setText("Ideal Goal: Not set");
        }

        updateElectricityPieChart(usage, goal);
        updateInsights(usage, goal);
    }

    private void updateInsights(Long usage, Long goal) {
        Long minGoal = usageViewModel.minElectricityGoal.getValue();
        Long maxGoal = usageViewModel.maxElectricityGoal.getValue();

        if (usage == null || minGoal == null || goal == null || maxGoal == null || goal == 0) {
            tvElectricityAlerts.setText("Alerts: Set a goal to see alerts.");
            tvElectricityAlerts.setTextColor(getThemeTextColor());
            return;
        }

        if (usage > maxGoal) {
            tvElectricityAlerts.setText("🚨 CRITICAL: Max limit exceeded!");
            tvElectricityAlerts.setTextColor(Color.RED);
        } else if (usage > goal || usage < minGoal) {
            tvElectricityAlerts.setText("⚠️ WARNING: Outside ideal range.");
            tvElectricityAlerts.setTextColor(Color.parseColor("#FFA000"));
        } else {
            tvElectricityAlerts.setText("✅ OPTIMAL: Usage is good.");
            tvElectricityAlerts.setTextColor(Color.parseColor("#388E3C"));
        }
    }

    private void updateElectricityPieChart(Long usage, Long goal) {
        if (usage == null || goal == null || goal == 0) {
            pieChartElectricity.clear();
            pieChartElectricity.invalidate();
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
        dataSet.setColors(Color.parseColor("#2196F3"), Color.parseColor("#BBDEFB"));
        dataSet.setValueTextColor(Color.TRANSPARENT);
        PieData pieData = new PieData(dataSet);

        pieChartElectricity.setUsePercentValues(true);
        pieChartElectricity.setData(pieData);
        pieChartElectricity.getDescription().setEnabled(false);
        pieChartElectricity.setHoleRadius(70f);
        pieChartElectricity.setCenterText(String.format(Locale.getDefault(), "%.0f%%\nUsed", rawPercent));
        pieChartElectricity.setCenterTextSize(18f);
        pieChartElectricity.setCenterTextColor(Color.parseColor("#1976D2"));
        pieChartElectricity.getLegend().setEnabled(true);
        pieChartElectricity.animateY(1000);
        pieChartElectricity.invalidate();
    }

    private void updateWeeklyBarChart(Map<String, Long> usageMap) {
        updateBarChart(barChartWeekly, usageMap, dayFormat, "Weekly Electricity Usage", Color.parseColor("#2196F3"), sdf);
    }

    private void updateMonthlyBarChart(Map<String, Long> usageMap) {
        updateBarChart(barChartMonthly, usageMap, monthYearFormat, "Monthly Electricity Usage", Color.parseColor("#3F51B5"), monthYearSdf);
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